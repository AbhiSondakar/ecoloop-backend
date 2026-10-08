package com.ecoloop.routing;

<<<<<<< HEAD
import com.ecoloop.device.Device;
import com.ecoloop.device.DeviceRepository;
import com.ecoloop.partner.Partner;
import com.ecoloop.partner.PartnerRepository;
import com.ecoloop.pickup.PickupRepository;
import com.ecoloop.pickup.PickupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
=======
import com.ecoloop.partner.Partner;
import com.ecoloop.partner.PartnerRepository;
import com.ecoloop.pickup.PickupRepository;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
<<<<<<< HEAD
import java.util.*;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private final RoutingOfferRepository offers;
    private final PartnerRepository partners;
    private final PickupRepository pickups;
    private final DeviceRepository devices;
    private final com.ecoloop.pickup.PickupService pickupService;

    public RoutingService(RoutingOfferRepository offers,
                          PartnerRepository partners,
                          PickupRepository pickups,
                          DeviceRepository devices,
                          com.ecoloop.pickup.PickupService pickupService) {
        this.offers = offers;
        this.partners = partners;
        this.pickups = pickups;
        this.devices = devices;
=======
import java.util.UUID;

@Service
public class RoutingService {
    private final RoutingOfferRepository offers;
    private final PartnerRepository partners;
    private final com.ecoloop.pickup.PickupService pickupService;

    public RoutingService(RoutingOfferRepository offers, PartnerRepository partners, com.ecoloop.pickup.PickupService pickupService) {
        this.offers = offers;
        this.partners = partners;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        this.pickupService = pickupService;
    }

    public double score(boolean capabilityMatch, double conditionFit, double distanceScore,
                        double ratingScore, double loadScore) {
<<<<<<< HEAD
        return .30 * (capabilityMatch ? 1.0 : 0.0)
=======
        return .30 * (capabilityMatch ? 1 : 0)
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
            + .20 * conditionFit
            + .20 * distanceScore
            + .15 * ratingScore
            + .15 * loadScore;
    }

    @Transactional
    public RoutingOffer acceptOffer(UUID partnerUserId, UUID offerId) {
        Partner partner = requirePartner(partnerUserId);
<<<<<<< HEAD
        if (!"approved".equalsIgnoreCase(partner.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Partner is not approved");
        }

        RoutingOffer offer = offers.findByIdAndPartnerIdForUpdate(offerId, partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        enforceOfferNotExpired(offer);

        if (!"offered".equalsIgnoreCase(offer.getStatus())) {
            throw new IllegalStateException("Offer is no longer available (current status: " + offer.getStatus() + ")");
        }

        offer.accept();
        RoutingOffer saved = offers.save(offer);
=======
        var offer = offers.findByIdAndPartnerId(offerId, partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        
        offer.accept();
        var saved = offers.save(offer);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        pickupService.acceptByPartnerUser(partnerUserId, saved.getPickupId());
        return saved;
    }

    @org.springframework.context.event.EventListener
    @Transactional
    public void onPickupCancelled(com.ecoloop.pickup.PickupCancelledEvent event) {
        cancelOffersForPickup(event.pickupId());
    }

    @org.springframework.context.event.EventListener
    @Transactional
    public void onPickupAccepted(com.ecoloop.pickup.PickupAcceptedEvent event) {
        for (RoutingOffer other : offers.findAllByPickupId(event.pickupId())) {
            if (!other.getPartnerId().equals(event.partnerId()) && "offered".equals(other.getStatus())) {
                other.setStatus("superseded");
                offers.save(other);
            }
        }
    }

    @org.springframework.context.event.EventListener
    @Transactional
    public void onPickupCreated(com.ecoloop.pickup.PickupCreatedEvent event) {
<<<<<<< HEAD
        createTopNOffers(event.pickupId());
    }

    public void createTopNOffers(UUID pickupId) {
        Optional<PickupRequest> pickupOpt = pickups.findById(pickupId);
        if (pickupOpt.isEmpty()) return;
        PickupRequest pickup = pickupOpt.get();

        String category = null;
        if (pickup.getDeviceId() != null) {
            category = devices.findById(pickup.getDeviceId()).map(Device::getCategory).orElse(null);
        }

        List<Partner> approvedPartners = partners.findAllByStatus("approved");
        List<ScoredPartner> candidates = new ArrayList<>();

        Map<UUID, Long> activeJobsByPartner = new HashMap<>();
        if (!approvedPartners.isEmpty()) {
            List<UUID> partnerIds = approvedPartners.stream().map(Partner::getId).toList();
            for (Object[] row : pickups.countActiveJobsByPartnerIds(partnerIds)) {
                activeJobsByPartner.put((UUID) row[0], (Long) row[1]);
            }
        }

        for (Partner p : approvedPartners) {
            long activeJobs = activeJobsByPartner.getOrDefault(p.getId(), 0L);
            if (activeJobs >= p.getCapacity()) {
                continue; // Partner is at full capacity
            }

            boolean capabilityMatch = p.getCapabilities() != null &&
                (category == null || p.getCapabilities().toLowerCase().contains(category.toLowerCase()));
            double loadScore = p.getCapacity() > 0 ? (1.0 - (double) activeJobs / p.getCapacity()) : 0.5;
            double ratingScore = p.getRating() != null ? Math.min(1.0, p.getRating().doubleValue() / 5.0) : 0.8;
            double totalScore = score(capabilityMatch, 1.0, 1.0, ratingScore, loadScore);

            candidates.add(new ScoredPartner(p, totalScore));
        }

        // Sort descending by score and pick top 5
        candidates.sort(Comparator.comparingDouble(ScoredPartner::score).reversed());
        List<ScoredPartner> topN = candidates.stream().limit(5).toList();

        for (ScoredPartner sp : topN) {
            if (!offers.existsByPickupIdAndPartnerIdAndStatusIn(
                    pickupId, sp.partner().getId(), List.of("offered", "accepted"))) {
                RoutingOffer offer = new RoutingOffer(
                    pickupId,
                    sp.partner().getId(),
                    Instant.now().plusSeconds(24 * 3600)
                );
                offer.setScore(sp.score());
                offers.save(offer);
            }
        }
        log.info("Dispatched {} routing offers for pickup: {}", topN.size(), pickupId);
=======
        partners.findAllByStatus("approved").forEach(partner ->
            offers.save(new com.ecoloop.routing.RoutingOffer(event.pickupId(), partner.getId(),
                Instant.now().plusSeconds(24 * 60 * 60))));
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    public void cancelOffersForPickup(UUID pickupId) {
        for (RoutingOffer offer : offers.findAllByPickupId(pickupId)) {
            if ("offered".equals(offer.getStatus()) || "pending".equals(offer.getStatus())) {
                offer.setStatus("cancelled");
                offers.save(offer);
            }
        }
    }

    @Transactional
    public RoutingOffer rejectOffer(UUID partnerUserId, UUID offerId) {
<<<<<<< HEAD
        return rejectOffer(partnerUserId, offerId, null);
    }

    @Transactional
    public RoutingOffer rejectOffer(UUID partnerUserId, UUID offerId, String reason) {
        Partner partner = requirePartner(partnerUserId);
        RoutingOffer offer = offers.findByIdAndPartnerIdForUpdate(offerId, partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        enforceOfferNotExpired(offer);

        if (!Set.of("offered", "accepted", "in_progress").contains(offer.getStatus())) {
            throw new IllegalStateException("Offer is no longer available (current status: " + offer.getStatus() + ")");
        }

        offer.reject(reason);
        log.info("Offer {} rejected by partner {}: reason={}", offerId, partner.getId(), reason);
=======
        Partner partner = requirePartner(partnerUserId);
        var offer = offers.findByIdAndPartnerId(offerId, partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        offer.reject();
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        return offers.save(offer);
    }

    private Partner requirePartner(UUID partnerUserId) {
        return partners.findByUserId(partnerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Partner profile not found"));
    }

    public UUID partnerIdForUser(UUID partnerUserId) {
        return requirePartner(partnerUserId).getId();
    }
<<<<<<< HEAD

    public void enforceOfferNotExpired(RoutingOffer offer) {
        if (offer.getExpiresAt() == null) {
            return;
        }
        Instant now = Instant.now();
        if (offer.getExpiresAt().isBefore(now) && "offered".equalsIgnoreCase(offer.getStatus())) {
            log.warn("Offer {} has expired (expiresAt={}, now={}); transitioning status to expired",
                offer.getId(), offer.getExpiresAt(), now);
            offer.setStatus("expired");
            offers.save(offer);
        }
        if (offer.getExpiresAt().isBefore(now)) {
            log.warn("Expired offer accessed: offerId={} status={} expiresAt={}",
                offer.getId(), offer.getStatus(), offer.getExpiresAt());
            throw new IllegalStateException("Offer has expired (expired at: " + offer.getExpiresAt() + ")");
        }
    }

    private record ScoredPartner(Partner partner, double score) {}
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
}
