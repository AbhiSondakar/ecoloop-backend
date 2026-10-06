package com.ecoloop.pickup;

import com.ecoloop.audit.AuditService;
import com.ecoloop.notification.NotificationService;
import com.ecoloop.partner.Partner;
import com.ecoloop.partner.PartnerRepository;
import com.ecoloop.rewards.RewardLedger;
import com.ecoloop.rewards.RewardLedgerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class PickupService {
    private final PickupRepository pickups;
    private final PartnerRepository partners;
    private final RewardLedgerRepository ledger;
    private final NotificationService notifications;
    private final AuditService audit;
    private final org.springframework.context.ApplicationEventPublisher events;
    private final com.ecoloop.device.DeviceRepository devices;

    public PickupService(PickupRepository pickups, PartnerRepository partners,
                         RewardLedgerRepository ledger, NotificationService notifications,
                         AuditService audit, org.springframework.context.ApplicationEventPublisher events,
                         com.ecoloop.device.DeviceRepository devices) {
        this.pickups = pickups;
        this.partners = partners;
        this.ledger = ledger;
        this.notifications = notifications;
        this.audit = audit;
        this.events = events;
        this.devices = devices;
    }

    @Transactional
    public PickupRequest createPickup(UUID userId, UUID deviceId, String address, Instant scheduledAt) {
        PickupRequest pickup = new PickupRequest(userId, deviceId, address);
        if (scheduledAt != null) pickup.setScheduledAt(scheduledAt);
        pickup.setCreatedAt(Instant.now());
        pickup.setUpdatedAt(Instant.now());
        pickup = pickups.save(pickup);
        events.publishEvent(new PickupCreatedEvent(pickup.getId()));
        return pickup;
    }

    @Transactional
    public PickupRequest cancelPickup(UUID userId, UUID pickupId) {
        var p = pickups.findByIdAndUserId(pickupId, userId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!Set.of("pending", "accepted").contains(p.getStatus())) {
            throw new IllegalStateException("Invalid pickup state transition");
        }
        p.setStatus("cancelled");
        p.setUpdatedAt(Instant.now());
        var saved = pickups.save(p);
        events.publishEvent(new PickupCancelledEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public PickupRequest cancelPickupByDevice(UUID userId, UUID deviceId) {
        PickupRequest pickup = pickups.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
            .filter(p -> deviceId.equals(p.getDeviceId())
                && !Set.of("completed", "cancelled").contains(p.getStatus()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No active pickup found for this device"));
        pickup.setStatus("cancelled");
        pickup.setUpdatedAt(Instant.now());
        var saved = pickups.save(pickup);
        events.publishEvent(new PickupCancelledEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public PickupRequest complete(UUID userId, UUID pickupId) {
        var pickup = pickups.findByIdAndUserId(pickupId, userId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        return completeAcceptedPickup(pickup, userId, "household");
    }
    public PickupRequest acceptByPartnerUser(UUID partnerUserId, UUID pickupId) {
        Partner partner = requirePartner(partnerUserId);
        PickupRequest pickup = pickups.findById(pickupId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!"pending".equals(pickup.getStatus())) {
            throw new IllegalStateException("Invalid pickup state transition");
        }
        pickup.setPartnerId(partner.getId());
        pickup.setStatus("accepted");
        pickup.setUpdatedAt(Instant.now());
        var saved = pickups.save(pickup);
        events.publishEvent(new PickupAcceptedEvent(saved.getId(), partner.getId()));
        return saved;
    }

    @Transactional
    public PickupRequest rejectByPartnerUser(UUID partnerUserId, UUID pickupId) {
        Partner partner = requirePartner(partnerUserId);
        PickupRequest pickup = pickups.findById(pickupId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (pickup.getPartnerId() != null && !partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Pickup is not assigned to this partner");
        }
        if (!Set.of("pending", "accepted", "in_progress").contains(pickup.getStatus())) {
            throw new IllegalStateException("Invalid pickup state transition");
        }
        pickup.setStatus("rejected");
        pickup.setUpdatedAt(Instant.now());
        return pickups.save(pickup);
    }

    @Transactional
    public PickupRequest completeForPartner(UUID partnerId, UUID pickupId) {
        var pickup = pickups.findById(pickupId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!partnerId.equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Pickup is not assigned to this partner");
        }
        return completeAcceptedPickup(pickup, pickup.getUserId(), "partner");
    }

    @Transactional
    public PickupRequest completeForPartnerUser(UUID partnerUserId, UUID pickupId) {
        Partner partner = requirePartner(partnerUserId);
        return completeForPartner(partner.getId(), pickupId);
    }

    private Partner requirePartner(UUID partnerUserId) {
        return partners.findByUserId(partnerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Partner profile not found"));
    }

    private PickupRequest completeAcceptedPickup(PickupRequest pickup, UUID householdId,
                                                  String actorRole) {
        if ("completed".equals(pickup.getStatus())) return pickup;
        if (!Set.of("accepted", "in_progress").contains(pickup.getStatus())) {
            throw new IllegalStateException("Pickup must be accepted before completion");
        }
        pickup.setStatus("completed");
        pickup.setCompletedAt(Instant.now());
        pickup.setUpdatedAt(Instant.now());
        var saved = pickups.save(pickup);
        if (ledger.findByUserIdAndReferenceId(householdId, pickup.getId()).isEmpty()) {
            ledger.save(new RewardLedger(householdId, 25, "earn",
                "Pickup completed reward", pickup.getId()));
            notifications.create(householdId, "pickup_completed",
                "Pickup completed", "You earned 25 points");
        }
        audit.record(householdId, actorRole, "pickup.completed",
            "pickup", pickup.getId(), "success");
        return saved;
    }

    public PickupWithDevice enrich(PickupRequest pickup) {
        if (pickup.getDeviceId() == null) return PickupWithDevice.from(pickup, null);
        var device = devices.findById(pickup.getDeviceId()).orElse(null);
        return PickupWithDevice.from(pickup, device);
    }

    public java.util.List<PickupWithDevice> enrich(java.util.List<PickupRequest> pickups) {
        return pickups.stream().map(this::enrich).toList();
    }
}
