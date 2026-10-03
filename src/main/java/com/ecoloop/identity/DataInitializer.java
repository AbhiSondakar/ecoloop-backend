package com.ecoloop.identity;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdminUser(UserRepository users,
                                    PasswordEncoder encoder,
                                    Environment env) {
        return args -> {
            String email = env.getProperty("ADMIN_EMAIL", "admin@ecoloop.local");
            String password = env.getProperty("ADMIN_PASSWORD", "Admin@123");

            if (users.findByEmailIgnoreCase(email).isPresent()) {
                return;
            }

            User admin = new User();
            admin.setId(UUID.randomUUID());
            admin.setEmail(email);
            admin.setPasswordHash(encoder.encode(password));
            admin.setName("System Administrator");
            admin.setRole(User.Role.ADMIN.name());
            admin.setActive(true);
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            users.save(admin);
        };
    }
}
