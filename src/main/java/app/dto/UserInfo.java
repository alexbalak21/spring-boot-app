package app.dto;

import app.security.CustomUserDetails;

public class UserInfo {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String createdAt;
    private String updatedAt;

    public UserInfo(CustomUserDetails user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getUsername();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt().toString();
        this.updatedAt = user.getUpdatedAt().toString();
    }

    // getters (and optionally setters if you want)
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}