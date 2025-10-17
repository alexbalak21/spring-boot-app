# Spring Security Configuration Guide

## SecurityConfig Class

### Purpose
This class configures security for your Spring Boot application, including:
- CORS (Cross-Origin Resource Sharing)
- CSRF (Cross-Site Request Forgery) protection
- Request authorization rules

## Key Components

### 1. CSRF Protection
```java
CookieCsrfTokenRepository.withHttpOnlyFalse();
```
- **What it does**: Protects against CSRF attacks
- **`withHttpOnlyFalse()`**: Allows JavaScript to read the CSRF token
- **`setCookiePath("/")`**: Makes token available across all paths

### 2. CORS Configuration
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```
- Enables CORS with custom configuration
- Allows cross-origin requests from specified origins

### 3. Request Authorization
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/", "/static/**").permitAll()
    .requestMatchers("/api/**").permitAll()
    .anyRequest().authenticated()
)
```
- Public access to static resources and API endpoints
- All other requests require authentication

### 4. CORS Settings
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:8100"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("X-XSRF-TOKEN", "Content-Type"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```
- **`allowedOrigins`**: Frontend URL
- **`allowedMethods`**: Supported HTTP methods
- **`allowCredentials`**: Enables sending cookies

## Troubleshooting

### CSRF Token Issues
- **Symptom**: 403 Forbidden errors
- **Fix**: Ensure:
  - Frontend sends `X-XSRF-TOKEN` header
  - CSRF token cookie is present
  - Token is included in non-GET requests

### CORS Problems
- **Symptom**: CORS policy errors in browser
- **Fix**: Check:
  - Allowed origins match frontend URL
  - Required headers are in `allowedHeaders`
  - `allowCredentials` is true if using cookies

## Production Notes
- Restrict allowed origins
- Use HTTPS
- Consider stricter security for API endpoints
