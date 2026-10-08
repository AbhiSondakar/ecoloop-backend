package com.ecoloop.partner;

<<<<<<< HEAD
import com.ecoloop.common.web.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

=======
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/partners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPartnerController {
<<<<<<< HEAD

    private final PartnerRepository partners;
    private final PartnerLifecycleService lifecycleService;

    public AdminPartnerController(PartnerRepository partners, PartnerLifecycleService lifecycleService) {
        this.partners = partners;
        this.lifecycleService = lifecycleService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageResponse<PartnerDto> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String search) {
        int boundedPage = Math.max(0, page);
        int boundedSize = Math.min(Math.max(1, size), 100);
        PageRequest pageable = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Partner> spec = Specification.where(PartnerSpecifications.withStatus(status))
                .and(PartnerSpecifications.withSearch(search));
        Page<Partner> partnerPage = partners.findAll(spec, pageable);
        Page<PartnerDto> dtoPage = partnerPage.map(p -> PartnerDto.from(p, true));
        return PageResponse.of(dtoPage);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public PartnerDto get(@PathVariable UUID id) {
        return partners.findById(id)
            .map(p -> PartnerDto.from(p, true))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public PartnerDto approve(@PathVariable UUID id) {
        return PartnerDto.from(lifecycleService.approve(id), true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public PartnerDto reject(@PathVariable UUID id) {
        return PartnerDto.from(lifecycleService.reject(id), true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/suspend")
    public PartnerDto suspend(@PathVariable UUID id) {
        return PartnerDto.from(lifecycleService.suspend(id), true);
    }
=======
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
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
}
