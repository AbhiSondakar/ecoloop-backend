package com.ecoloop.identity;

<<<<<<< HEAD
import com.ecoloop.common.RateLimiterService;
import com.ecoloop.common.SessionUser;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Value;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
<<<<<<< HEAD
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
=======
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final IdentityService identityApi;
<<<<<<< HEAD
    private final RateLimiterService rateLimiter;
    private final PasswordResetService passwordResetService;

    @Value("${ecoloop.security.trusted-proxies:}")
    private String trustedProxies;

    public AuthController(AuthenticationManager authManager,
                          UserRepository users,
                          PasswordEncoder encoder,
                          IdentityService identityApi,
                          RateLimiterService rateLimiter,
                          PasswordResetService passwordResetService) {
=======

    public AuthController(AuthenticationManager authManager, UserRepository users,
                          PasswordEncoder encoder, IdentityService identityApi) {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        this.authManager = authManager;
        this.users = users;
        this.encoder = encoder;
        this.identityApi = identityApi;
<<<<<<< HEAD
        this.rateLimiter = rateLimiter;
        this.passwordResetService = passwordResetService;
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    public record RegisterRequest(
        @NotBlank @Email String email,
<<<<<<< HEAD
        @NotBlank @Size(min = 12, max = 128) String password,
=======
        @NotBlank @Size(min = 8) String password,
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        @NotBlank String name,
        String phone,
        String address) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public IdentityService.UserDto register(@Valid @RequestBody RegisterRequest req,
                                            HttpServletRequest httpReq,
                                            HttpServletResponse httpResp) {
<<<<<<< HEAD
        String ip = clientIp(httpReq);
        rateLimiter.checkLimit("register-ip", ip, 5, Duration.ofMinutes(1));

        String normalizedEmail = User.normalizeEmail(req.email());
        PasswordPolicy.validate(req.password());

        if (users.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail(normalizedEmail);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setName(req.name().trim());
        if (req.phone() != null) u.setPhone(req.phone().trim());
        if (req.address() != null) u.setAddress(req.address().trim());
        u.setRole(User.Role.HOUSEHOLD.name());
        u.setActive(true);
=======
        if (users.findByEmail(req.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setName(req.name());
        if (req.phone() != null) u.setPhone(req.phone());
        if (req.address() != null) u.setAddress(req.address());
        u.setRole(User.Role.HOUSEHOLD.name());
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        users.save(u);

<<<<<<< HEAD
        log.info("User registered successfully: userId={} role=HOUSEHOLD", u.getId());
        establishSession(normalizedEmail, req.password(), httpReq);
=======
        log.info("User registered: email={} role=HOUSEHOLD", u.getEmail());
        establishSession(req.email(), req.password(), u, httpReq, httpResp);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        return identityApi.toDto(u);
    }

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {}

    @PostMapping("/login")
    public ResponseEntity<IdentityService.UserDto> login(@Valid @RequestBody LoginRequest req,
                                                         HttpServletRequest httpReq,
                                                         HttpServletResponse httpResp) {
<<<<<<< HEAD
        String ip = clientIp(httpReq);
        String normalizedEmail = User.normalizeEmail(req.email());

        rateLimiter.checkLimit("login-ip", ip, 10, Duration.ofMinutes(1));
        rateLimiter.checkLimit("login-account", normalizedEmail, 5, Duration.ofMinutes(1));

        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, req.password()));

        User u = users.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!u.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled");
        }

        saveSession(auth, httpReq);
        log.info("Login successful: userId={} role={}", u.getId(), u.getRole());
=======
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User u = users.findByEmail(req.email()).orElseThrow();
        saveSession(auth, u, httpReq, httpResp);
        log.info("Login successful: email={} role={}", u.getEmail(), u.getRole());
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        return ResponseEntity.ok(identityApi.toDto(u));
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
<<<<<<< HEAD
        if (token == null) {
            return Map.of("token", "", "headerName", "X-XSRF-TOKEN", "parameterName", "_csrf");
        }
=======
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
<<<<<<< HEAD
        if (s != null) {
            s.invalidate();
        }
        SecurityContextHolder.clearContext();
        log.info("User session invalidated on logout");
=======
        String sessionId = s != null ? s.getId() : "-";
        if (s != null) s.invalidate();
        log.info("Logout: session={}", sessionId);
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
<<<<<<< HEAD
    public void forgotPassword(@RequestBody Map<String, String> body, HttpServletRequest httpReq) {
        String ip = clientIp(httpReq);
        rateLimiter.checkLimit("forgot-ip", ip, 5, Duration.ofMinutes(1));

        String rawEmail = body.get("email");
        if (rawEmail != null && !rawEmail.isBlank()) {
            passwordResetService.requestPasswordReset(rawEmail);
        }
        log.info("Password reset request processed");
    }

    public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 12, max = 128) String newPassword) {}

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
=======
    public void forgotPassword(@RequestBody Map<String, String> body) {
        log.info("Forgot password requested for email: {}", body.get("email"));
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }

    @GetMapping("/me")
    public IdentityService.UserDto me(HttpServletRequest r) {
<<<<<<< HEAD
        SessionUser user = SessionUser.require(r);
        return identityApi.findById(user.id());
    }

    private void establishSession(String email, String password, HttpServletRequest httpReq) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
        saveSession(auth, httpReq);
    }

    private void saveSession(Authentication auth, HttpServletRequest httpReq) {
        HttpSession oldSession = httpReq.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = httpReq.getSession(true);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
    }

    private String clientIp(HttpServletRequest req) {
        String remoteAddress = normalizeIpLiteral(req.getRemoteAddr()).orElse("unknown");
        if (!isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }

        String forwardedFor = req.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return remoteAddress;
        }

        String[] hops = forwardedFor.split(",");
        for (int i = hops.length - 1; i >= 0; i--) {
            Optional<String> hop = normalizeIpLiteral(hops[i]);
            if (hop.isPresent() && !isTrustedProxy(hop.get())) {
                return hop.get();
            }
        }
        return remoteAddress;
    }

    private boolean isTrustedProxy(String address) {
        if (trustedProxies == null || trustedProxies.isBlank()) {
            return false;
        }
        return Arrays.stream(trustedProxies.split(","))
            .map(this::normalizeIpLiteral)
            .flatMap(Optional::stream)
            .anyMatch(address::equals);
    }

    private Optional<String> normalizeIpLiteral(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String candidate = value.trim();
        if (candidate.startsWith("[") && candidate.endsWith("]")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        if (!isIpLiteral(candidate)) {
            return Optional.empty();
        }

        try {
            return Optional.of(InetAddress.getByName(candidate).getHostAddress());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private boolean isIpLiteral(String value) {
        if (value.contains(":")) {
            return value.matches("[0-9a-fA-F:.]+") && value.matches(".*[0-9a-fA-F].*");
        }

        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (!octet.matches("\\d{1,3}")) {
                return false;
            }
            try {
                if (Integer.parseInt(octet) > 255) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
=======
        HttpSession s = r.getSession(false);
        if (s == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        SecurityContext ctx = (SecurityContext) s.getAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (ctx == null || ctx.getAuthentication() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        String email = ctx.getAuthentication().getName();
        return identityApi.findByEmail(email);
    }

    private void establishSession(String email, String password, User u,
                                  HttpServletRequest httpReq, HttpServletResponse httpResp) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
        saveSession(auth, u, httpReq, httpResp);
    }

    private void saveSession(Authentication auth, User u,
                             HttpServletRequest httpReq, HttpServletResponse httpResp) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        HttpSession session = httpReq.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
        session.setAttribute("USER_ID", u.getId().toString());
        session.setAttribute("ROLE", u.getRole());
        httpResp.setHeader("X-Session-Id", session.getId());
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
    }
}
