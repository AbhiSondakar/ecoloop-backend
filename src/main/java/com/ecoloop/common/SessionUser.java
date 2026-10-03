package com.ecoloop.common;

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
}
