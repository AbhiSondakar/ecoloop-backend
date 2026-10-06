package com.ecoloop.pickup;

import com.ecoloop.partner.PartnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/pickups")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPickupController {
  private final PickupRepository pickups;
  private final PartnerRepository partners;
  private final PickupService pickupService;

  public AdminPickupController(PickupRepository pickups, PartnerRepository partners, PickupService pickupService) {
    this.pickups = pickups;
    this.partners = partners;
    this.pickupService = pickupService;
  }

  @GetMapping
  public List<PickupWithDevice> list() {
    return pickupService.enrich(pickups.findAll());
  }

  public record ReassignRequest(UUID partnerId) {}

  @PostMapping("/{id}/reassign")
  public PickupWithDevice reassign(@PathVariable UUID id,
                                @RequestBody(required = false) ReassignRequest body) {
    PickupRequest pickup = pickups.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pickup not found"));
    if (body != null && body.partnerId() != null) {
      partners.findById(body.partnerId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));
      pickup.setPartnerId(body.partnerId());
    } else {
      pickup.setPartnerId(null);
    }
    pickup.setUpdatedAt(Instant.now());
    return pickupService.enrich(pickups.save(pickup));
  }
}
