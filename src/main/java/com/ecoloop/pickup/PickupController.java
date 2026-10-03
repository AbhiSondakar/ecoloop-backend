package com.ecoloop.pickup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/pickups")
public class PickupController {
    private static final Logger log = LoggerFactory.getLogger(PickupController.class);

    private final PickupRepository pickups;
    private final PickupService pickupService;

    public PickupController(PickupRepository p, PickupService pickupService) {
        this.pickups = p;
        this.pickupService = pickupService;
    }

    private UUID user(HttpServletRequest r) {
        var s = r.getSession(false);
        if (s == null || s.getAttribute("USER_ID") == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return UUID.fromString(String.valueOf(s.getAttribute("USER_ID")));
    }

    public record CreatePickup(
        UUID deviceId,
        @NotBlank String address,
        Instant scheduledAt) {}

    @PostMapping
    public PickupRequest create(@Valid @RequestBody CreatePickup body, HttpServletRequest r) {
        PickupRequest pickup = pickupService.createPickup(user(r), body.deviceId(), body.address(), body.scheduledAt());
        log.info("Pickup created: id={} user={} device={}", pickup.getId(), user(r), body.deviceId());
        return pickup;
    }

    @GetMapping
    public List<PickupRequest> list(HttpServletRequest r) {
        return pickups.findAllByUserIdOrderByCreatedAtDesc(user(r));
    }

    @GetMapping("/{id}")
    public PickupRequest get(@PathVariable UUID id, HttpServletRequest r) {
        return pickups.findByIdAndUserId(id, user(r))
            .orElseThrow(() -> new NoSuchElementException("Pickup not found"));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('PARTNER')")
    public PickupRequest accept(@PathVariable UUID id, HttpServletRequest r) {
        UUID actor = user(r);
        PickupRequest result = pickupService.acceptByPartnerUser(actor, id);
        log.info("Pickup accepted: id={} by partner={}", id, actor);
        return result;
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('PARTNER')")
    public PickupRequest complete(@PathVariable UUID id, HttpServletRequest r) {
        UUID actor = user(r);
        PickupRequest result = pickupService.completeForPartnerUser(actor, id);
        log.info("Pickup completed: id={} by partner={}", id, actor);
        return result;
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('PARTNER')")
    public PickupRequest reject(@PathVariable UUID id, HttpServletRequest r) {
        UUID actor = user(r);
        PickupRequest result = pickupService.rejectByPartnerUser(actor, id);
        log.info("Pickup rejected: id={} by partner={}", id, actor);
        return result;
    }

    @PostMapping("/{id}/cancel")
    public PickupRequest cancel(@PathVariable UUID id, HttpServletRequest r) {
        PickupRequest saved = pickupService.cancelPickup(user(r), id);
        log.info("Pickup cancelled: id={} user={}", id, user(r));
        return saved;
    }
}
