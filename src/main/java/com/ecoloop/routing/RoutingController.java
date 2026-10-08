package com.ecoloop.routing;

import com.ecoloop.common.SessionUser;
import com.ecoloop.pickup.PickupService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
<<<<<<< HEAD
import org.springframework.security.access.prepost.PreAuthorize;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routing")
public class RoutingController {
    private final RoutingOfferRepository offers;
    private final RoutingService routingService;
    private final PickupService pickupService;

    public RoutingController(RoutingOfferRepository offers, RoutingService routingService,
                             PickupService pickupService) {
        this.offers = offers;
        this.routingService = routingService;
        this.pickupService = pickupService;
    }

<<<<<<< HEAD
    @PreAuthorize("hasRole('PARTNER')")
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    @GetMapping("/offers")
    public List<RoutingOffer> offers(HttpServletRequest r) {
        return offers.findAllByPartnerIdOrderByCreatedAtDesc(partnerId(r));
    }

<<<<<<< HEAD
    @PreAuthorize("hasRole('PARTNER')")
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    @PostMapping("/offers/{id}/accept")
    public RoutingOffer accept(@PathVariable UUID id, HttpServletRequest r) {
        return routingService.acceptOffer(partnerUserId(r), id);
    }

<<<<<<< HEAD
    @PreAuthorize("hasRole('PARTNER')")
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    @PostMapping("/offers/{id}/reject")
    public RoutingOffer reject(@PathVariable UUID id, HttpServletRequest r) {
        return routingService.rejectOffer(partnerUserId(r), id);
    }

<<<<<<< HEAD
    @PreAuthorize("hasRole('PARTNER')")
    @PostMapping("/offers/{id}/complete")
    public Object complete(@PathVariable UUID id, HttpServletRequest r) {
        UUID partnerUserId = partnerUserId(r);
        UUID partnerId = routingService.partnerIdForUser(partnerUserId);
=======
    @PostMapping("/offers/{id}/complete")
    public Object complete(@PathVariable UUID id, HttpServletRequest r) {
        UUID partnerId = partnerId(r);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        var offer = offers.findByIdAndPartnerId(id, partnerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        if (!"accepted".equals(offer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Offer must be accepted before completion");
        }
<<<<<<< HEAD
        return pickupService.completeForPartnerUser(partnerUserId, offer.getPickupId());
=======
        return pickupService.completeForPartner(partnerId, offer.getPickupId());
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    private UUID partnerUserId(HttpServletRequest r) {
        var sessionUser = SessionUser.require(r);
        sessionUser.requireRole("partner");
        return sessionUser.id();
    }

    private UUID partnerId(HttpServletRequest r) {
        return routingService.partnerIdForUser(partnerUserId(r));
    }
}
