package app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping
    public String home(HttpServletResponse response) {
        response.setContentType("application/json");
        return "{\"message\": \"Hello from Spring Boot\"}";
    }

    @PostMapping("/demo")
    public String postMethodName(@RequestBody String entity, HttpServletResponse response) {
        response.setContentType("application/json");
        return entity;
    }
    
}