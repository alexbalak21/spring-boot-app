package app.security;

import app.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wraps your User entity and exposes it as a Spring Security UserDetails.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Expose extra fields for your controllers/DTOs
    public Long getId() {
        return user.getId();
    }

    public String getName() {
        return user.getName();
    }

    public String getRole() {
        return user.getRole();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getCreatedAt() {
        return user.getCreatedAt().toString();
    }

    public String getUpdatedAt() {
        return user.getUpdatedAt().toString();
    }

    // --- UserDetails contract ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Prefix with ROLE_ to match Spring conventions
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security uses "username" as the login identifier
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // customize if you track expiry
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // customize if you track locks
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // customize if you track credential expiry
    }

    @Override
    public boolean isEnabled() {
        return true; // customize if you track active/inactive users
    }
}
