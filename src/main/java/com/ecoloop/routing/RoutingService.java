package com.ecoloop.routing;

import com.ecoloop.partner.Partner;
import com.ecoloop.partner.PartnerRepository;
import com.ecoloop.pickup.PickupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class RoutingService {
    private final RoutingOfferRepository offers;
    private final PartnerRepository partners;
    private final com.ecoloop.pickup.PickupService pickupService;

    public RoutingService(RoutingOfferRepository offers, PartnerRepository partners, com.ecoloop.pickup.PickupService pickupService) {
        this.offers = offers;
        this.partners = partners;
        this.pickupService = pickupService;
    }

    public double score(boolean capabilityMatch, double conditionFit, double distanceScore,
                        double ratingScore, double loadScore) {
        return .30 * (capabilityMatch ? 1 : 0)
            + .20 * conditionFit
            + .20 * distanceScore
            + .15 * ratingScore
            + .15 * loadScore;
    }

    @Transactional
    public RoutingOffer acceptOffer(UUID partnerUserId, UUID offerId) {
        Partner partner = requirePartner(partnerUserId);
        var offer = offers.findByIdAndPartnerId(offerId, partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        
        offer.accept();
        var saved = offers.save(offer);
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
        partners.findAllByStatus("approved").forEach(partner ->
            offers.save(new com.ecoloop.routing.RoutingOffer(event.pickupId(), partner.getId(),
                Instant.now().plusSeconds(24 * 60 * 60))));
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
        Partner partner = requirePartner(partnerUserId);
        var offer = offers.findByIdAndPartnerId(offerId, partner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        offer.reject();
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
}
