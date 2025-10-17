package app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
    
    @GetMapping("/api/csrf")
    public ResponseEntity<Void> getCsrfToken() {
        // The CSRF token will be automatically set in the response cookie
        // by the CsrfTokenRepository configured in SecurityConfig
        return ResponseEntity.ok().build();
    }
}