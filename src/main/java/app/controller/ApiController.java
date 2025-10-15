package app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/users")
public class ApiController {
    private final List<User> users = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    // Create
    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setId(counter.incrementAndGet());
        users.add(user);
        return user;
    }

    // Read all
    @GetMapping
    public List<User> getAllUsers() {
        return users;
    }

    // Read one
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .map(user -> {
                    user.setName(updatedUser.getName());
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // User class
    public static class User {
        private Long id;
        private String name;

        // Default constructor for JSON deserialization
        public User() {}

        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}