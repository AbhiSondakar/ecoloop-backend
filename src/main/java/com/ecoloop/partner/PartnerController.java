package com.ecoloop.partner;

import com.ecoloop.common.SessionUser;
<<<<<<< HEAD
import com.ecoloop.common.upload.FileStorageService;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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
<<<<<<< HEAD
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
=======
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
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

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
<<<<<<< HEAD
    private final FileStorageService fileStorageService;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c

    public PartnerController(PartnerRepository partners, UserRepository users,
                             RoutingOfferRepository offers, PickupRepository pickups,
                             RewardLedgerRepository ledger, IdentityService identityService,
<<<<<<< HEAD
                             RoutingService routingService, PickupService pickupService,
                             FileStorageService fileStorageService) {
=======
                             RoutingService routingService, PickupService pickupService) {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        this.partners = partners;
        this.users = users;
        this.offers = offers;
        this.pickups = pickups;
        this.ledger = ledger;
        this.identityService = identityService;
        this.routingService = routingService;
        this.pickupService = pickupService;
<<<<<<< HEAD
        this.fileStorageService = fileStorageService;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    public record Registration(
        @NotBlank String orgName,
        @NotBlank String type,
        String licenseNo,
        String serviceAreas,
        String capabilities) {}

    @PostMapping
<<<<<<< HEAD
    public PartnerDto register(@Valid @RequestBody Registration body, HttpServletRequest request) {
=======
    public Partner register(@Valid @RequestBody Registration body, HttpServletRequest request) {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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
<<<<<<< HEAD
        log.info("Partner application submitted: user={}", sessionUser.id());
        return PartnerDto.from(partner, true);
    }

    @PostMapping(value = "/license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadLicense(@RequestPart("license") MultipartFile license, HttpServletRequest request) throws IOException {
        var sessionUser = SessionUser.require(request);
        Partner partner = partners.findByUserId(sessionUser.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner profile not found"));

        FileStorageService.StoredFile stored = fileStorageService.storeFile(sessionUser.id(), "licenses", license, true);
        partner.setLicenseUploadId(stored.metadata().getId());
        partner.setUpdatedAt(Instant.now());
        partners.save(partner);

        log.info("Partner license uploaded: partnerId={}", partner.getId());
        return Map.of("uploadId", stored.metadata().getId(), "url", stored.publicUri());
=======
        log.info("Partner application submitted: org={} user={}. Awaiting admin approval to update role to PARTNER.", body.orgName(), sessionUser.id());
        return partner;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @GetMapping("/offers")
    @PreAuthorize("hasRole('PARTNER')")
    public List<RoutingOffer> offers(HttpServletRequest request) {
        Partner partner = currentPartner(request);
<<<<<<< HEAD
        return offers.findAllByPartnerIdOrderByCreatedAtDesc(partner.getId());
=======
        var result = offers.findAllByPartnerIdOrderByCreatedAtDesc(partner.getId());
        log.debug("Partner offers list: partner={} count={}", partner.getId(), result.size());
        return result;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('PARTNER')")
    public List<com.ecoloop.pickup.PickupWithDevice> jobs(HttpServletRequest request) {
        Partner partner = currentPartner(request);
        return pickupService.enrich(pickups.findAllByPartnerId(partner.getId()));
    }

    @GetMapping("/me")
<<<<<<< HEAD
    @PreAuthorize("hasRole('PARTNER')")
    public PartnerDto me(HttpServletRequest request) {
        return PartnerDto.from(currentPartner(request), true);
=======
    public Partner me(HttpServletRequest request) {
        return currentPartner(request);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    public record PartnerUpdate(
        String orgName,
        String serviceAreas,
        String capabilities,
        Integer capacity) {}

    @PatchMapping("/me")
    @PreAuthorize("hasRole('PARTNER')")
<<<<<<< HEAD
    public PartnerDto updateMe(@RequestBody PartnerUpdate body, HttpServletRequest request) {
=======
    public Partner updateMe(@RequestBody PartnerUpdate body, HttpServletRequest request) {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        Partner partner = currentPartner(request);
        if (body.orgName() != null) partner.setOrgName(body.orgName());
        if (body.serviceAreas() != null) partner.setServiceAreas(body.serviceAreas());
        if (body.capabilities() != null) partner.setCapabilities(body.capabilities());
        if (body.capacity() != null) partner.setCapacity(body.capacity());
        partner.setUpdatedAt(Instant.now());
<<<<<<< HEAD
        return PartnerDto.from(partners.save(partner), true);
    }

    @GetMapping("/{id}")
    public PartnerDto get(@PathVariable UUID id, HttpServletRequest request) {
        SessionUser user = SessionUser.require(request);
        Partner partner = partners.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Partner not found"));
        boolean canSeeSensitive = "ADMIN".equalsIgnoreCase(user.role()) || user.id().equals(partner.getUserId());
        return PartnerDto.from(partner, canSeeSensitive);
=======
        return partners.save(partner);
    }

    @GetMapping("/{id}")
    public Partner get(@PathVariable UUID id) {
        return partners.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Partner not found"));
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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
<<<<<<< HEAD
        long activeJobs = pickups.countActiveJobsByPartnerId(partnerId);
=======
        long activeJobs = pickups.findAllByPartnerId(partnerId).stream()
            .filter(p -> !"completed".equals(p.getStatus()) && !"cancelled".equals(p.getStatus()))
            .count();
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
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

<<<<<<< HEAD
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("offersToday", offersToday);
        result.put("activeJobs", activeJobs);
        result.put("monthlyCompletions", monthlyCompletions);
        result.put("pointsBalance", pointsBalance);
        result.put("nextOfferAt", nextOfferAt != null ? nextOfferAt.toString() : null);
        result.put("capacityUsed", activeJobs);
        result.put("capacityTotal", partner.getCapacity());
        return result;
=======
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("offersToday", offersToday);
        kpis.put("activeJobs", activeJobs);
        kpis.put("monthlyCompletions", monthlyCompletions);
        kpis.put("pointsBalance", pointsBalance);
        kpis.put("nextOfferAt", nextOfferAt != null ? nextOfferAt.toString() : null);
        kpis.put("capacityUsed", activeJobs);
        kpis.put("capacityTotal", partner.getCapacity());
        return kpis;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasRole('PARTNER')")
    public com.ecoloop.pickup.PickupWithDevice job(@PathVariable UUID id, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        PickupRequest pickup = pickups.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this partner");
        }
        return pickupService.enrich(pickup);
    }

    @PostMapping("/offers/{id}/accept")
    @PreAuthorize("hasRole('PARTNER')")
<<<<<<< HEAD
=======
    @org.springframework.transaction.annotation.Transactional
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    public RoutingOffer acceptOffer(@PathVariable UUID id, HttpServletRequest request) {
        return routingService.acceptOffer(SessionUser.require(request).id(), id);
    }

    @PostMapping("/offers/{id}/reject")
    @PreAuthorize("hasRole('PARTNER')")
<<<<<<< HEAD
    public RoutingOffer rejectOffer(@PathVariable UUID id,
                                    @RequestBody(required = false) Map<String, String> body,
                                    HttpServletRequest request) {
        String reason = body != null ? body.get("reason") : null;
        return routingService.rejectOffer(SessionUser.require(request).id(), id, reason);
=======
    public RoutingOffer rejectOffer(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        return routingService.rejectOffer(SessionUser.require(request).id(), id);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @PostMapping("/jobs/{id}/verify")
    @PreAuthorize("hasRole('PARTNER')")
<<<<<<< HEAD
    public com.ecoloop.pickup.PickupWithDevice verifyJob(@PathVariable UUID id,
                                                         @RequestBody Map<String, Object> body,
                                                         HttpServletRequest request) {
        String category = body.get("category") != null ? body.get("category").toString() : null;
        String condition = body.get("condition") != null ? body.get("condition").toString() : null;
        String notes = body.get("notes") != null ? body.get("notes").toString() : null;
        String evidenceUrl = body.get("evidenceUrl") != null ? body.get("evidenceUrl").toString() : null;

        PickupRequest pickup = pickupService.verifyForPartner(SessionUser.require(request).id(), id,
            category, condition, notes, evidenceUrl);
        return pickupService.enrich(pickup);
=======
    public com.ecoloop.pickup.PickupWithDevice verifyJob(@PathVariable UUID id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        PickupRequest pickup = pickups.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
        if (!partner.getId().equals(pickup.getPartnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this partner");
        }
        pickup.setStatus("in_progress");
        pickup.setUpdatedAt(Instant.now());
        return pickupService.enrich(pickups.save(pickup));
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @PostMapping("/jobs/{id}/complete")
    @PreAuthorize("hasRole('PARTNER')")
    public com.ecoloop.pickup.PickupWithDevice completeJob(@PathVariable UUID id, HttpServletRequest request) {
        Partner partner = currentPartner(request);
        return pickupService.enrich(pickupService.completeForPartner(partner.getId(), id));
    }

    private Partner currentPartner(HttpServletRequest request) {
        var sessionUser = SessionUser.require(request);
<<<<<<< HEAD
        Partner partner = partners.findByUserId(sessionUser.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner profile not found"));
        if (!"approved".equalsIgnoreCase(partner.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Partner application is not approved");
        }
        return partner;
=======
        return partners.findByUserId(sessionUser.id())
            .orElseThrow(() -> new NoSuchElementException("Partner profile not found"));
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }
}
