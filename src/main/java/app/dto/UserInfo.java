package app.dto;

import app.security.CustomUserDetails;
import org.springframework.security.core.userdetails.User;

public class UserInfo {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String createdAt;
    private String updatedAt;

    // Construct from your CustomUserDetails
    public UserInfo(CustomUserDetails user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getUsername();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        this.updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null;
    }

    // Construct from Spring’s default User
    public UserInfo(User springUser) {
        this.id = null; // Spring’s User doesn’t carry an id
        this.name = null; // no name field
        this.email = springUser.getUsername();
        this.role = springUser.getAuthorities().toString();
        this.createdAt = null;
        this.updatedAt = null;
    }

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
