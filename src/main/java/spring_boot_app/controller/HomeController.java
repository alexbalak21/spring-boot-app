package spring_boot_app.controller;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    public String home(HttpServletResponse response) {
        response.setContentType("application/json");
        return "{\"message\": \"Hello from Spring Boot API\"}";
    }
    
}
