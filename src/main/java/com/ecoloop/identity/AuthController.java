package com.ecoloop.identity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final IdentityService identityApi;

    public AuthController(AuthenticationManager authManager, UserRepository users,
                          PasswordEncoder encoder, IdentityService identityApi) {
        this.authManager = authManager;
        this.users = users;
        this.encoder = encoder;
        this.identityApi = identityApi;
    }

    public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String name,
        String phone,
        String address) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public IdentityService.UserDto register(@Valid @RequestBody RegisterRequest req,
                                            HttpServletRequest httpReq,
                                            HttpServletResponse httpResp) {
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
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        users.save(u);

        log.info("User registered: email={} role=HOUSEHOLD", u.getEmail());
        establishSession(req.email(), req.password(), u, httpReq, httpResp);
        return identityApi.toDto(u);
    }

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {}

    @PostMapping("/login")
    public ResponseEntity<IdentityService.UserDto> login(@Valid @RequestBody LoginRequest req,
                                                         HttpServletRequest httpReq,
                                                         HttpServletResponse httpResp) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User u = users.findByEmail(req.email()).orElseThrow();
        saveSession(auth, u, httpReq, httpResp);
        log.info("Login successful: email={} role={}", u.getEmail(), u.getRole());
        return ResponseEntity.ok(identityApi.toDto(u));
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        String sessionId = s != null ? s.getId() : "-";
        if (s != null) s.invalidate();
        log.info("Logout: session={}", sessionId);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@RequestBody Map<String, String> body) {
        log.info("Forgot password requested for email: {}", body.get("email"));
    }

    @GetMapping("/me")
    public IdentityService.UserDto me(HttpServletRequest r) {
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
    }
}
