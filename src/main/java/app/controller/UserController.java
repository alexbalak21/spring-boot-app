package app.controller;

import app.dto.UserInfo;

import app.security.CustomUserDetails;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {


    @GetMapping("/user")
    public ResponseEntity<UserInfo> currentUser(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new UserInfo(user));
    }
}
