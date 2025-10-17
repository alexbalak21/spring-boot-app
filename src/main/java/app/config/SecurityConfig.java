package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Marks this class as a configuration source for Spring
public class SecurityConfig {

    // 🔐 Defines the security filter chain for handling HTTP security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🛡️ Enables CSRF protection and stores the token in a cookie
            // ⚠️ withHttpOnlyFalse() allows frontend JavaScript to read the token from document.cookie
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            );
        // ✅ Builds and returns the configured security filter chain
        return http.build();
    }

    // 🌍 Configures CORS rules for Spring MVC controllers
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Applies CORS rules to /api/** endpoints
                        .allowedOrigins("http://localhost:8100") // Allows requests from this origin (your frontend)
                        .allowedMethods("GET", "POST") // Permits only GET and POST methods
                        .allowCredentials(true); // Allows cookies and credentials to be sent with requests
            }
        };
    }
}
