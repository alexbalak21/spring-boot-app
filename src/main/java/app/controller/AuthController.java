package app.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/auth")
public class AuthController {
    

    @PostMapping("/login")
    public String login(@RequestBody String request, HttpServletResponse response) {
        //TODO: process POST request
        response.setContentType("application/json");
        return "{\"message\": \"Login successful\"}";
    }
    
}
