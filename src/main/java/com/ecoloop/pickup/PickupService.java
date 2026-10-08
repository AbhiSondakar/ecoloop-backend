package com.ecoloop.pickup;

import com.ecoloop.audit.AuditService;
<<<<<<< HEAD
import com.ecoloop.classification.api.ClassificationApi;
import com.ecoloop.classification.api.ClassificationResult;
import com.ecoloop.common.upload.FileStorageService;
import com.ecoloop.device.Device;
import com.ecoloop.device.DeviceRepository;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import com.ecoloop.notification.NotificationService;
import com.ecoloop.partner.Partner;
import com.ecoloop.partner.PartnerRepository;
import com.ecoloop.rewards.RewardLedger;
import com.ecoloop.rewards.RewardLedgerRepository;
<<<<<<< HEAD
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
=======
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class PickupService {
<<<<<<< HEAD

    private static final Logger log = LoggerFactory.getLogger(PickupService.class);

=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    private final PickupRepository pickups;
    private final PartnerRepository partners;
    private final RewardLedgerRepository ledger;
    private final NotificationService notifications;
    private final AuditService audit;
<<<<<<< HEAD
    private final ApplicationEventPublisher events;
    private final DeviceRepository devices;
    private final FileStorageService fileStorageService;
    private final ClassificationApi classificationApi;
    private final TransactionTemplate transactionTemplate;

    public PickupService(PickupRepository pickups,
                         PartnerRepository partners,
                         RewardLedgerRepository ledger,
                         NotificationService notifications,
                         AuditService audit,
                         ApplicationEventPublisher events,
                         DeviceRepository devices,
                         FileStorageService fileStorageService,
                         ClassificationApi classificationApi,
                         PlatformTransactionManager transactionManager) {
=======
    private final org.springframework.context.ApplicationEventPublisher events;
    private final com.ecoloop.device.DeviceRepository devices;

    public PickupService(PickupRepository pickups, PartnerRepository partners,
                         RewardLedgerRepository ledger, NotificationService notifications,
                         AuditService audit, org.springframework.context.ApplicationEventPublisher events,
                         com.ecoloop.device.DeviceRepository devices) {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        this.pickups = pickups;
        this.partners = partners;
        this.ledger = ledger;
        this.notifications = notifications;
        this.audit = audit;
        this.events = events;
        this.devices = devices;
<<<<<<< HEAD
        this.fileStorageService = fileStorageService;
        this.classificationApi = classificationApi;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @Transactional
    public PickupRequest createPickup(UUID userId, UUID deviceId, String address, Instant scheduledAt) {
<<<<<<< HEAD
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID is required");
        }

        Device device = devices.findById(deviceId)
            .orElseThrow(() -> new NoSuchElementException("Device not found"));

        if (!userId.equals(device.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device does not belong to this user");
        }

        if (pickups.findActiveByDeviceId(deviceId).isPresent()) {
            throw new IllegalStateException("Device already has an active pickup request");
        }

        PickupRequest pickup = new PickupRequest(userId, deviceId, address != null ? address.trim() : "");
        if (scheduledAt != null) {
            pickup.setScheduledAt(scheduledAt);
        }
=======
        PickupRequest pickup = new PickupRequest(userId, deviceId, address);
        if (scheduledAt != null) pickup.setScheduledAt(scheduledAt);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        pickup.setCreatedAt(Instant.now());
        pickup.setUpdatedAt(Instant.now());
        pickup = pickups.save(pickup);
        events.publishEvent(new PickupCreatedEvent(pickup.getId()));
        return pickup;
    }

<<<<<<< HEAD
    public PickupWithDevice submitHouseholdPickup(UUID userId,
                                                  MultipartFile image,
                                                  String condition,
                                                  String address,
                                                  Instant scheduledAt) throws IOException {
        FileStorageService.StoredFile stored = fileStorageService.storeFile(userId, "devices", image, false);
        String mime = stored.metadata().getContentType();
        String imageUrl = stored.publicUri();
        UUID deviceId = UUID.randomUUID();

        ClassificationResult result = classificationApi.classify(stored.content(), mime, deviceId, imageUrl);

        if ("failed".equals(result.status())) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, 
                "AI classification failed due to a backend issue. Please try again later."
            );
        }

        if (result.confidence() == 0.0 && !result.requiresManualReview()) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, 
                "AI model could not classify the image. Please provide a clearer picture."
            );
        }

        return transactionTemplate.execute(status -> {
            Device device = new Device();
            device.setId(deviceId);
            device.setUserId(userId);
            device.setCondition(condition != null ? condition.trim() : "good");
            device.setImageUrl(imageUrl);
            device.setCreatedAt(Instant.now());
            device.setUpdatedAt(Instant.now());
            device.setCategory(result.category());
            device.setAiCategory(result.category());
            device.setAiConfidence(BigDecimal.valueOf(result.confidence()));
            device.setAiProvider(result.provider());
            device.setAiStatus(result.deviceAiStatus());
            device = devices.save(device);

            PickupRequest pickup = new PickupRequest(userId, device.getId(), address != null ? address.trim() : "");
            if (scheduledAt != null) {
                pickup.setScheduledAt(scheduledAt);
            }
            pickup.setCreatedAt(Instant.now());
            pickup.setUpdatedAt(Instant.now());
            pickup = pickups.save(pickup);

            events.publishEvent(new PickupCreatedEvent(pickup.getId()));
            log.info("Atomic household pickup created: pickupId={} deviceId={} category={}",
                    pickup.getId(), device.getId(), result.category());

            return PickupWithDevice.from(pickup, device);
        });
    }

    @Transactional
    public PickupRequest cancelPickup(UUID userId, UUID pickupId) {
        PickupRequest p = pickups.findByIdAndUserId(pickupId, userId)
=======
    @Transactional
    public PickupRequest cancelPickup(UUID userId, UUID pickupId) {
        var p = pickups.findByIdAndUserId(pickupId, userId)
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!Set.of("pending", "accepted").contains(p.getStatus())) {
            throw new IllegalStateException("Invalid pickup state transition");
        }
        p.setStatus("cancelled");
        p.setUpdatedAt(Instant.now());
<<<<<<< HEAD
        PickupRequest saved = pickups.save(p);
=======
        var saved = pickups.save(p);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        events.publishEvent(new PickupCancelledEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public PickupRequest cancelPickupByDevice(UUID userId, UUID deviceId) {
<<<<<<< HEAD
        PickupRequest pickup = pickups.findActiveByUserIdAndDeviceId(userId, deviceId)
            .orElseThrow(() -> new NoSuchElementException("No active pickup found for this device"));
        pickup.setStatus("cancelled");
        pickup.setUpdatedAt(Instant.now());
        PickupRequest saved = pickups.save(pickup);
=======
        PickupRequest pickup = pickups.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
            .filter(p -> deviceId.equals(p.getDeviceId())
                && !Set.of("completed", "cancelled").contains(p.getStatus()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No active pickup found for this device"));
        pickup.setStatus("cancelled");
        pickup.setUpdatedAt(Instant.now());
        var saved = pickups.save(pickup);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        events.publishEvent(new PickupCancelledEvent(saved.getId()));
        return saved;
    }

    @Transactional
    public PickupRequest complete(UUID userId, UUID pickupId) {
<<<<<<< HEAD
        PickupRequest pickup = pickups.findByIdAndUserId(pickupId, userId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        return completeAcceptedPickup(pickup, userId, "household");
    }

    @Transactional(timeout = 5)
    public PickupRequest acceptByPartnerUser(UUID partnerUserId, UUID pickupId) {
        Partner partner = requirePartner(partnerUserId);
        if (!"approved".equalsIgnoreCase(partner.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Partner is not approved");
        }

        Partner lockedPartner = partners.findByIdForUpdate(partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Partner profile not found during lock acquisition"));
        if (!"approved".equalsIgnoreCase(lockedPartner.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Partner is not approved");
        }

        long activeJobs = pickups.countActiveJobsByPartnerId(lockedPartner.getId());
        if (activeJobs >= lockedPartner.getCapacity()) {
            throw new IllegalStateException("Partner has reached maximum active capacity");
        }

        PickupRequest pickup = pickups.findByIdForUpdate(pickupId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));

        if (!"pending".equals(pickup.getStatus())) {
            throw new IllegalStateException("Pickup is no longer pending (current: " + pickup.getStatus() + ")");
        }

        pickup.setPartnerId(lockedPartner.getId());
        pickup.setStatus("accepted");
        pickup.setUpdatedAt(Instant.now());
        PickupRequest saved = pickups.save(pickup);
        events.publishEvent(new PickupAcceptedEvent(saved.getId(), lockedPartner.getId()));
=======
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
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        return saved;
    }

    @Transactional
    public PickupRequest rejectByPartnerUser(UUID partnerUserId, UUID pickupId) {
        Partner partner = requirePartner(partnerUserId);
<<<<<<< HEAD
        PickupRequest pickup = pickups.findByIdForUpdate(pickupId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));

        if (!partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Pickup is not assigned to this partner");
        }

        if (!Set.of("accepted", "in_progress").contains(pickup.getStatus())) {
            throw new IllegalStateException("Invalid pickup state transition");
        }

        // Return pickup back to pending and clear partner assignment
        pickup.setStatus("pending");
        pickup.setPartnerId(null);
        pickup.setUpdatedAt(Instant.now());
        PickupRequest saved = pickups.save(pickup);

        // Re-publish pickup event so other candidate partners can receive offers
        events.publishEvent(new PickupCreatedEvent(saved.getId()));
        return saved;
=======
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
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @Transactional
    public PickupRequest completeForPartner(UUID partnerId, UUID pickupId) {
<<<<<<< HEAD
        PickupRequest pickup = pickups.findByIdForUpdate(pickupId)
=======
        var pickup = pickups.findById(pickupId)
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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

<<<<<<< HEAD
    @Transactional
    public PickupRequest verifyForPartner(UUID partnerUserId, UUID pickupId,
                                           String category, String condition, String notes,
                                           String evidenceUrl) {
        Partner partner = requirePartner(partnerUserId);
        PickupRequest pickup = pickups.findByIdForUpdate(pickupId)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));

        if (!partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Pickup is not assigned to this partner");
        }
        if (!Set.of("accepted", "in_progress").contains(pickup.getStatus())) {
            throw new IllegalStateException("Pickup must be accepted or in progress before verification");
        }

        pickup.verify(category, condition, notes, evidenceUrl, partner.getId());
        return pickups.save(pickup);
    }

    private Partner requirePartner(UUID partnerUserId) {
        Partner partner = partners.findByUserId(partnerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Partner profile not found"));
        if (!"approved".equalsIgnoreCase(partner.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Partner is not approved");
        }
        return partner;
=======
    private Partner requirePartner(UUID partnerUserId) {
        return partners.findByUserId(partnerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Partner profile not found"));
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    private PickupRequest completeAcceptedPickup(PickupRequest pickup, UUID householdId,
                                                  String actorRole) {
<<<<<<< HEAD
        if ("completed".equals(pickup.getStatus())) {
            return pickup;
        }

        if (!Set.of("accepted", "in_progress", "verified").contains(pickup.getStatus())) {
            throw new IllegalStateException("Pickup must be accepted, in progress, or verified before completion");
        }

        pickup.setStatus("completed");
        pickup.setCompletedAt(Instant.now());
        pickup.setUpdatedAt(Instant.now());
        PickupRequest saved = pickups.save(pickup);

        try {
            if (ledger.findByUserIdAndReferenceId(householdId, pickup.getId()).isEmpty()) {
                ledger.save(new RewardLedger(householdId, 25, "earn",
                    "Pickup completed reward", pickup.getId()));
                notifications.create(householdId, "pickup_completed",
                    "Pickup completed", "You earned 25 points");
            }
        } catch (DataIntegrityViolationException ex) {
            log.info("Reward already awarded for pickup: {}", pickup.getId());
        }

=======
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
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        audit.record(householdId, actorRole, "pickup.completed",
            "pickup", pickup.getId(), "success");
        return saved;
    }

    public PickupWithDevice enrich(PickupRequest pickup) {
<<<<<<< HEAD
        if (pickup.getDeviceId() == null) {
            return PickupWithDevice.from(pickup, null);
        }
        Device device = devices.findById(pickup.getDeviceId()).orElse(null);
        return PickupWithDevice.from(pickup, device);
    }

    public List<PickupWithDevice> enrich(List<PickupRequest> pickupList) {
        return pickupList.stream().map(this::enrich).toList();
=======
        if (pickup.getDeviceId() == null) return PickupWithDevice.from(pickup, null);
        var device = devices.findById(pickup.getDeviceId()).orElse(null);
        return PickupWithDevice.from(pickup, device);
    }

    public java.util.List<PickupWithDevice> enrich(java.util.List<PickupRequest> pickups) {
        return pickups.stream().map(this::enrich).toList();
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }
}
