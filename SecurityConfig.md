### SecurityConfig Documentation

#### Overview

This document describes the SecurityConfig class used in a Spring Boot servlet application to configure CORS, CSRF, request authorization, and an origin-check filter. The configuration is tailored for a single-page application (SPA) frontend running at http://localhost:8100 and exposes a specific endpoint (/api/csrf) as a CSRF-exempt target. It provides the full code for the SecurityConfig class plus an in-depth explanation of each part, edge cases, and hardening recommendations.

* * *

### [SecurityConfig.java](https://SecurityConfig.java) (full source)

    package app.config;
    
    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
    import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
    import org.springframework.security.web.csrf.CsrfFilter;
    import org.springframework.security.web.util.matcher.RequestMatcher;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.filter.OncePerRequestFilter;
    
    import java.io.IOException;
    import java.util.Arrays;
    import java.util.Collections;
    
    @Configuration
    public class SecurityConfig {
    
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            // CSRF token repo with cookie readable by JS (for SPA)
            CookieCsrfTokenRepository tokenRepository = new CookieCsrfTokenRepository();
            tokenRepository.setCookieCustomizer(cookie -> cookie
                .httpOnly(false)    // allow JS to read XSRF cookie
                .secure(true)       // only send over HTTPS in prod
                .sameSite("Strict") // tighten cross-site leakage; relax to Lax if needed
                .path("/")          // scope
            );
    
            CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
            requestHandler.setCsrfRequestAttributeName("_csrf");
    
            // Lambda-based RequestMatcher: ignore CSRF for POST /api/csrf
            RequestMatcher csrfIgnore = request -> {
                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                    return false;
                }
                String uri = request.getRequestURI(); // includes context path if present
                return "/api/csrf".equals(uri);
            };
    
            http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                    .csrfTokenRepository(tokenRepository)
                    .csrfTokenRequestHandler(requestHandler)
                    .ignoringRequestMatchers(csrfIgnore)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/static/**").permitAll()
                    .requestMatchers("/api/**").permitAll()
                    .anyRequest().authenticated()
                )
                .addFilterBefore(originCheckFilter(), CsrfFilter.class);
    
            return http.build();
        }
    
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8100"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            configuration.setAllowedHeaders(Arrays.asList("X-XSRF-TOKEN", "Content-Type", "Authorization"));
            configuration.setExposedHeaders(Collections.singletonList("X-XSRF-TOKEN"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
    
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    
        @Bean
        public OncePerRequestFilter originCheckFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    String origin = request.getHeader("Origin");
                    String referer = request.getHeader("Referer");
    
                    if (request.getMethod().matches("POST|PUT|DELETE")) {
                        if ((origin != null && !origin.equals("http://localhost:8100")) ||
                            (referer != null && !referer.startsWith("http://localhost:8100"))) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid origin");
                            return;
                        }
                    }
    
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

* * *

### What the class does

*   **Defines a SecurityFilterChain bean** that configures HTTP security for the application.
    
*   **Stores CSRF tokens in a cookie** accessible to JavaScript so an SPA can read and send the token back in requests.
    
*   **Excludes a single endpoint** (POST /api/csrf) from CSRF checks using a custom RequestMatcher implemented as a lambda.
    
*   **Enables CORS** with a CorsConfigurationSource tailored to the SPA origin.
    
*   **Permits public access** to root, static resources, and all /api/\*\* endpoints while requiring authentication for any other request.
    
*   **Adds an originCheckFilter** before the CsrfFilter to validate Origin and Referer headers on mutating requests.
    

* * *

### Detailed explanation of each part

#### CSRF token repository

*   **CookieCsrfTokenRepository** stores CSRF tokens in a cookie the frontend can read.
    
*   **Settings used**
    
    *   **httpOnly=false**: allows frontend JavaScript to read the cookie value to include in request headers.
        
    *   **secure=true**: cookie is sent only over HTTPS; keep true in production.
        
    *   **sameSite=Strict**: prevents cookie from being sent on most cross-site requests; relax to Lax if needed for some SPA patterns.
        
    *   **path=/**: cookie applies to the whole application.
        

#### CsrfTokenRequestAttributeHandler

*   Exposes the CSRF token under a request attribute named **\_csrf** so controllers, views, or filters can access it.
    

#### CSRF ignore matcher (lambda RequestMatcher)

*   Matches only requests that:
    
    *   Use HTTP method **POST**.
        
    *   Have `request.getRequestURI()` exactly equal to **/api/csrf**.
        
*   When matched, Spring Security will skip CSRF validation for that request.
    

#### CORS configuration

*   **Allowed origin**: http://localhost:8100.
    
*   **Allowed methods**: GET, POST, PUT, DELETE, OPTIONS, PATCH.
    
*   **Allowed headers**: X-XSRF-TOKEN, Content-Type, Authorization.
    
*   **Exposed headers**: X-XSRF-TOKEN so browser JS can read the header from responses.
    
*   **Allow credentials**: true to allow cookies and HTTP auth.
    
*   **Max age**: 3600 seconds for preflight cache.
    

#### Authorization rules

*   **"/" and "/static/**"\*\* are public.
    
*   **"/api/**"\*\* is currently permitted to everyone; review and tighten for sensitive endpoints.
    
*   **anyRequest()** requires authentication.
    

#### originCheckFilter

*   A OncePerRequestFilter run before CsrfFilter.
    
*   For mutating methods (POST, PUT, DELETE) it inspects Origin and Referer.
    
*   If **Origin** exists and is not exactly http://localhost:8100, or **Referer** exists and does not start with http://localhost:8100, the filter returns 403 Forbidden and halts processing.
    
*   Missing headers are treated permissively unless an existing header fails the check.
    

* * *

### Edge cases and important notes

*   **Context path**: If the app runs under a context path (for example `/app`), `getRequestURI()` returns `/app/api/csrf`. The lambda matcher compares against `/api/csrf` and will not match. Adjust using `request.getContextPath()` or use `endsWith("/api/csrf")`.
    
*   **Trailing slashes**: `/api/csrf/` will not match the exact equality check. Normalize or use `endsWith`/regex if you need to accept both.
    
*   **Absent Origin/Referer**: Some clients or proxies strip these headers. The originCheckFilter only enforces checks when a header exists and fails the check. If you need strict enforcement, change the logic to require a header and reject if missing.
    
*   **Excluding endpoints from CSRF**: Only exempt endpoints that are safe without a token, such as an endpoint that exclusively returns a new token. Never exempt state-changing endpoints without other protections.
    
*   **CORS credentials**: `allowCredentials(true)` combined with specific allowed origins is required for cookies to be included by browsers. Do not use `*` for allowed origins with credentials enabled.
    
*   **Development vs production**: keep `secure=true` for production. For local HTTP development, set secure conditionally based on profile.
    

* * *

### Hardening recommendations

*   **Narrow /api/** permitAll\*\* rule\*\*: Replace broad `requestMatchers("/api/**").permitAll()` with fine-grained rules for authenticated and public endpoints.
    
*   **Context-path aware matching**: Normalize URIs or use suffix/wildcard matching if you deploy with a context path.
    
*   **Token lifecycle**: Ensure CSRF token issuance and refreshing follow a clear lifecycle and that the frontend reads and sends tokens reliably.
    
*   **CSP and HSTS**: Add Content Security Policy and HSTS headers in production to reduce exposure from XSS and downgrade attacks.
    
*   **Strict Referer/Origin policy**: If acceptable, require Origin or Referer for mutating requests and reject when they are missing.
    
*   **Logging and monitoring**: Log invalid origin/referer attempts and monitor for suspicious patterns.
    
*   **Review cookie flags**: Consider `sameSite=Lax` if Strict breaks legitimate SPA flows like some OAuth redirects.
    

* * *

### How to adapt

*   **Match multiple paths**: Replace lambda logic with a Set lookup or a regex:
    
    *   Example: [`Set.of`](https://Set.of)`("/api/csrf","/internal/csrf").contains(uri)`
        
    *   Example: [`Pattern.compile`](https://Pattern.compile)`("^/api/(csrf|other)$").matcher(uri).matches()`
        
*   **Wildcard matching**: Use `uri.startsWith("/api/")` or regex to match patterns like `/api/**`.
    
*   **Context path**: Use [`uri.equals`](https://uri.equals)`(request.getContextPath() + "/api/csrf")` or `uri.endsWith("/api/csrf")`.
    
*   **Dev conditional secure cookie**: Toggle `secure(true)` based on active Spring profile.