package com.ecoloop.identity;

import com.ecoloop.rewards.RewardLedgerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IdentityService implements UserDetailsService {
    private final UserRepository users;
    private final RewardLedgerRepository rewardLedgerRepository;

    public IdentityService(UserRepository users, RewardLedgerRepository rewardLedgerRepository) {
        this.users = users;
        this.rewardLedgerRepository = rewardLedgerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = users.findByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException("User not found: " + email));
        return new org.springframework.security.core.userdetails.User(
            u.getEmail(),
            u.getPasswordHash(),
            u.isActive(),
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
    }

    public UserDto findByEmail(String email) {
        User u = users.findByEmail(email).orElseThrow(() ->
            new UsernameNotFoundException("User not found: " + email));
        return toDto(u);
    }

    public UserDto findById(UUID id) {
        User u = users.findById(id).orElseThrow(() ->
            new UsernameNotFoundException("User not found: " + id));
        return toDto(u);
    }

    public UserDto toDto(User u) {
        return new UserDto(
            u.getId(),
            u.getEmail(),
            u.getName(),
            u.getPhone(),
            u.getAddress(),
            u.getRole(),
            u.isActive(),
            u.getCreatedAt() != null ? u.getCreatedAt().toString() : null,
            rewardLedgerRepository.balance(u.getId()));
    }

    public record UserDto(
        UUID id,
        String email,
        String name,
        String phone,
        String address,
        String role,
        boolean active,
        String createdAt,
        int pointsBalance) {}
}
