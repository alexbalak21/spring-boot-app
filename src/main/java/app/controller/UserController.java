package app.controller;

import app.dto.UserInfo;
import app.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/user")
    public ResponseEntity<UserInfo> currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).build();
        }

        Object principal = authentication.getPrincipal();
        log.info("Authenticated principal type: {}", principal.getClass().getName());

        if (principal instanceof CustomUserDetails custom) {
            return ResponseEntity.ok(new UserInfo(custom));
        } else if (principal instanceof User springUser) {
            // ✅ Use the constructor that accepts Spring’s User
            return ResponseEntity.ok(new UserInfo(springUser));
        } else {
            return ResponseEntity.status(403).build();
        }
    }
}
