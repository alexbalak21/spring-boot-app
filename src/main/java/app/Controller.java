package app;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;


@RestController
public class Controller {

    @GetMapping("/api")
    public String home(HttpServletResponse response) {
        response.setContentType("application/json");
        return "{\"message\": \"Hello from Spring Boot API\"}";
    }
    
    
}
