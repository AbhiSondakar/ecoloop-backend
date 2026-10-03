package com.ecoloop.identity;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  public record RoleUpdate(String role) {}
  private final UserRepository users;
  private final IdentityService identity;
  public AdminUserController(UserRepository users, IdentityService identity) {
    this.users = users;
    this.identity = identity;
  }
  @GetMapping public List<IdentityService.UserDto> list() {
    return users.findAll().stream().map(identity::toDto).toList();
  }
  @PostMapping("/{id}/deactivate") public IdentityService.UserDto deactivate(@PathVariable UUID id) { return change(id, false); }
  @PostMapping("/{id}/activate") public IdentityService.UserDto activate(@PathVariable UUID id) { return change(id, true); }
  @PatchMapping("/{id}/role")
  public IdentityService.UserDto changeRole(@PathVariable UUID id, @RequestBody RoleUpdate body) {
    var user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (body == null || body.role() == null || body.role().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
    }
    try {
      user.setRole(User.Role.valueOf(body.role().trim().toUpperCase()).name());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
    }
    return identity.toDto(users.save(user));
  }
  private IdentityService.UserDto change(UUID id, boolean active) {
    var user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    user.setActive(active);
    return identity.toDto(users.save(user));
  }
}
