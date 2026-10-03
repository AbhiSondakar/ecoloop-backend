package com.ecoloop.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Logs every handled request (method, URI, query, status, latency, actor) and records an
 * {@link AuditLog} entry for mutating /api requests so "what task was performed" and
 * "the flow of each request" are observable. Lives in the isolated {@code audit} module and
 * depends only on the servlet API + Spring Security framework classes, reading actor identity
 * from the session attributes ({@code USER_ID}/{@code ROLE}) set by {@code AuthController}.
 */
@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger("ecoloop.audit");

    private final AuditService auditService;

    public AuditLoggingInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute("ecoloop.audit.start", System.nanoTime());
        log.info("{} {}?{}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString() == null ? "" : request.getQueryString());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Long start = (Long) request.getAttribute("ecoloop.audit.start");
        long ms = start == null ? 0L : (System.nanoTime() - start) / 1_000_000;
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("{} {} status={} latency={}ms actor={} role={}",
                method, uri, status, ms, principalName(request), roleName(request));

        if (ex != null) {
            log.warn("Request {} {} completed with exception: {}", method, uri, ex.toString());
        }

        if (uri.startsWith("/api/") && isMutating(method)) {
            recordAudit(request, status);
        }
    }

    private void recordAudit(HttpServletRequest request, int status) {
        try {
            UUID actorId = userId(request);
            String role = roleName(request);

            String[] segments = request.getRequestURI().replaceFirst("^/api/", "").split("/", 0);
            String entityType = segments.length > 0 && !segments[0].isBlank() ? segments[0] : null;
            UUID entityId = extractId(segments);

            String action = request.getMethod() + " " + request.getRequestURI();
            String result = status >= 500 ? "server_error"
                    : (status >= 400 ? "client_error" : "success");

            auditService.record(actorId, role, action, entityType, entityId, result);
        } catch (Exception e) {
            log.warn("Audit recording failed for {} {}: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
        }
    }

    private static boolean isMutating(String method) {
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }

    private static String principalName(HttpServletRequest request) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && a.getPrincipal() != null
                && !"anonymousUser".equals(a.getPrincipal())) {
            return a.getName();
        }
        HttpSession s = request.getSession(false);
        if (s != null) {
            Object uid = s.getAttribute("USER_ID");
            if (uid != null) return uid.toString();
        }
        return "anonymous";
    }

    private static String roleName(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null) {
            Object r = s.getAttribute("ROLE");
            if (r != null) return r.toString();
        }
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && a.getPrincipal() != null
                && !"anonymousUser".equals(a.getPrincipal())) {
            return a.getAuthorities().toString();
        }
        return null;
    }

    private static UUID userId(HttpServletRequest request) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && a.getPrincipal() != null
                && !"anonymousUser".equals(a.getPrincipal())) {
            try {
                return UUID.fromString(a.getName());
            } catch (IllegalArgumentException ignored) { }
        }
        HttpSession s = request.getSession(false);
        if (s != null) {
            Object uid = s.getAttribute("USER_ID");
            if (uid != null) {
                try {
                    return UUID.fromString(uid.toString());
                } catch (IllegalArgumentException ignored) { }
            }
        }
        return null;
    }

    private static UUID extractId(String[] segments) {
        if (segments.length < 2) return null;
        String last = segments[segments.length - 1];
        try {
            return UUID.fromString(last);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
