package com.ecoloop.partner;

import com.ecoloop.common.SessionUser;
import com.ecoloop.identity.IdentityService;
import com.ecoloop.identity.UserRepository;
import com.ecoloop.pickup.PickupRepository;
import com.ecoloop.pickup.PickupRequest;
import com.ecoloop.pickup.PickupService;
import com.ecoloop.routing.RoutingOffer;
import com.ecoloop.routing.RoutingOfferRepository;
import com.ecoloop.routing.RoutingService;
import com.ecoloop.rewards.RewardLedgerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {
    private static final Logger log = LoggerFactory.getLogger(PartnerController.class);

    private final PartnerRepository partners;
    private final UserRepository users;
    private final RoutingOfferRepository offers;
    private final PickupRepository pickups;
    private final RewardLedgerRepository ledger;
    private final IdentityService identityService;
    private final RoutingService routingService;
    private final PickupService pickupService;

    public PartnerController(PartnerRepository partners, UserRepository users,
                             RoutingOfferRepository offers, PickupRepository pickups,
                             RewardLedgerRepository ledger, IdentityService identityService,
                             RoutingService routingService, PickupService pickupService) {
        this.partners = partners;
        this.users = users;
        this.offers = offers;
        this.pickups = pickups;
        this.ledger = ledger;
        this.identityService = identityService;
        this.routingService = routingService;
        this.pickupService = pickupService;
    }

    public record Registration(
        @NotBlank String orgName,
        @NotBlank String type,
        String licenseNo,
        String serviceAreas,
        String capabilities) {}

    @PostMapping
    public Partner register(@Valid @RequestBody Registration body, HttpServletRequest request) {
        var sessionUser = SessionUser.require(request);
        if (partners.findByUserId(sessionUser.id()).isPresent()) {
            throw new IllegalStateException("Partner profile already exists");
        }
        Partner partner = new Partner(sessionUser.id(), body.orgName(), body.type(), body.licenseNo());
        if (body.serviceAreas() != null) partner.setServiceAreas(body.serviceAreas());
        if (body.capabilities() != null) partner.setCapabilities(body.capabilities());
        partner.setCreatedAt(Instant.now());
        partner.setUpdatedAt(Instant.now());
        partner = partners.save(partner);
        log.info("Partner application submitted: org={} user={}. Awaiting admin approval to update role to PARTNER.", body.orgName(), sessionUser.id());
        return partner;
    }

    @GetMapping("/offers")
    @PreAuthorize("hasRole('PARTNER')")
    public List<RoutingOffer> offers(HttpServletRequest request) {
        Partner partner = currentPartner(request);
        var result = offers.findAllByPartnerIdOrderByCreatedAtDesc(partner.getId());
        log.debug("Partner offers list: partner={} count={}", partner.getId(), result.size());
        return result;
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('PARTNER')")
    public List<PickupRequest> jobs(HttpServletRequest request) {
        Partner partner = currentPartner(request);
        return pickups.findAllByPartnerId(partner.getId());
    }

    @GetMapping("/me")
    public Partner me(HttpServletRequest request) {
        return currentPartner(request);
    }

    public record PartnerUpdate(
        String orgName,
        String serviceAreas,
        String capabilities,
        Integer capacity) {}

    @PatchMapping("/me")
    @PreAuthorize("hasRole('PARTNER')")
    public Partner updateMe(@RequestBody PartnerUpdate body, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        if (body.orgName() != null) partner.setOrgName(body.orgName());
        if (body.serviceAreas() != null) partner.setServiceAreas(body.serviceAreas());
        if (body.capabilities() != null) partner.setCapabilities(body.capabilities());
        if (body.capacity() != null) partner.setCapacity(body.capacity());
        partner.setUpdatedAt(Instant.now());
        return partners.save(partner);
    }

    @GetMapping("/{id}")
    public Partner get(@PathVariable UUID id) {
        return partners.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Partner not found"));
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasRole('PARTNER')")
    public Map<String, Object> kpis(HttpServletRequest request) {
        Partner partner = currentPartner(request);
        UUID partnerId = partner.getId();
        Instant today = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        Instant monthStart = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MONTHS);

        long offersToday = offers.findAllByPartnerIdOrderByCreatedAtDesc(partnerId).stream()
            .filter(o -> o.getCreatedAt().isAfter(today)).count();
        long activeJobs = pickups.findAllByPartnerId(partnerId).stream()
            .filter(p -> !"completed".equals(p.getStatus()) && !"cancelled".equals(p.getStatus()))
            .count();
        long monthlyCompletions = pickups.findAllByPartnerId(partnerId).stream()
            .filter(p -> "completed".equals(p.getStatus()))
            .filter(p -> p.getCompletedAt() != null && p.getCompletedAt().isAfter(monthStart))
            .count();
        int pointsBalance = ledger.balance(partner.getUserId());
        Instant nextOfferAt = offers.findAllByPartnerIdOrderByCreatedAtDesc(partnerId).stream()
            .filter(o -> "offered".equals(o.getStatus()))
            .map(RoutingOffer::getExpiresAt)
            .filter(Objects::nonNull)
            .min(Instant::compareTo)
            .orElse(null);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("offersToday", offersToday);
        kpis.put("activeJobs", activeJobs);
        kpis.put("monthlyCompletions", monthlyCompletions);
        kpis.put("pointsBalance", pointsBalance);
        kpis.put("nextOfferAt", nextOfferAt != null ? nextOfferAt.toString() : null);
        kpis.put("capacityUsed", activeJobs);
        kpis.put("capacityTotal", partner.getCapacity());
        return kpis;
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasRole('PARTNER')")
    public PickupRequest job(@PathVariable UUID id, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        PickupRequest pickup = pickups.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this partner");
        }
        return pickup;
    }

    @PostMapping("/offers/{id}/accept")
    @PreAuthorize("hasRole('PARTNER')")
    @org.springframework.transaction.annotation.Transactional
    public RoutingOffer acceptOffer(@PathVariable UUID id, HttpServletRequest request) {
        return routingService.acceptOffer(SessionUser.require(request).id(), id);
    }

    @PostMapping("/offers/{id}/reject")
    @PreAuthorize("hasRole('PARTNER')")
    public RoutingOffer rejectOffer(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        return routingService.rejectOffer(SessionUser.require(request).id(), id);
    }

    @PostMapping("/jobs/{id}/verify")
    @PreAuthorize("hasRole('PARTNER')")
    public PickupRequest verifyJob(@PathVariable UUID id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        PickupRequest pickup = pickups.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this partner");
        }
        pickup.setStatus("in_progress");
        pickup.setUpdatedAt(Instant.now());
        return pickups.save(pickup);
    }

    @PostMapping("/jobs/{id}/complete")
    @PreAuthorize("hasRole('PARTNER')")
    public PickupRequest completeJob(@PathVariable UUID id, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        return pickupService.completeForPartner(partner.getId(), id);
    }

    private Partner currentPartner(HttpServletRequest request) {
        var sessionUser = SessionUser.require(request);
        return partners.findByUserId(sessionUser.id())
            .orElseThrow(() -> new NoSuchElementException("Partner profile not found"));
    }
}
