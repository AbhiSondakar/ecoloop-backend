package com.ecoloop.partner;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/partners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPartnerController {
  private final PartnerRepository partners;
  public AdminPartnerController(PartnerRepository partners){this.partners=partners;}
  @GetMapping public List<Partner> list(){return partners.findAll();}
  @GetMapping("/{id}")
  public Partner get(@PathVariable UUID id) {
    return partners.findById(id)
      .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));
  }
  @PostMapping("/{id}/approve") public Partner approve(@PathVariable UUID id){return change(id,"approved");}
  @PostMapping("/{id}/reject") public Partner reject(@PathVariable UUID id){return change(id,"rejected");}
  @PostMapping("/{id}/suspend") public Partner suspend(@PathVariable UUID id){return change(id,"suspended");}
  private Partner change(UUID id,String status){var p=partners.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Partner not found"));p.setStatus(status);return partners.save(p);}
}
