package com.ecoloop.common;

<<<<<<< HEAD
import com.ecoloop.identity.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

public record SessionUser(UUID id, String role) {

    public static SessionUser require() {
        return current().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    public static SessionUser require(HttpServletRequest request) {
        return require();
    }

    public static Optional<SessionUser> current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof UserPrincipal up) {
            return Optional.of(new SessionUser(up.getId(), up.getRole()));
        }
        return Optional.empty();
    }

    public void requireRole(String expected) {
        if (!expected.equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }
    }
=======
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

public record SessionUser(UUID id, String role) {
  public static SessionUser require(HttpServletRequest request) {
    var session = request.getSession(false);
    if (session == null || session.getAttribute("USER_ID") == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    return new SessionUser(UUID.fromString(String.valueOf(session.getAttribute("USER_ID"))), String.valueOf(session.getAttribute("ROLE")));
  }

  public void requireRole(String expected) {
    if (!expected.equalsIgnoreCase(role)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
    }
  }
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
}
