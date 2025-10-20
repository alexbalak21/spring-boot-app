package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import jakarta.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    RequestMatcher csrfIgnore = request -> {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI(); // includes context path trimmed by container
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
            .requestMatchers(
                "/",
                "/index.html",
                "/static/**",
                "/assets/**",
                "/*.js",
                "/*.css",
                "/*.json",
                "/*.png",
                "/*.jpg",
                "/*.jpeg",
                "/*.gif",
                "/*.svg",
                "/*.ico",
                "/favicon.ico",
                "/error"
            ).permitAll()
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(originCheckFilter(), CsrfFilter.class);

    return http.build();
}
    
  
// Replace originCheckFilter() with this logging version (or add a second bean)
@Bean
public OncePerRequestFilter originCheckFilterWithLogging() {
    Logger log = LoggerFactory.getLogger("OriginCheckFilter");
    return new OncePerRequestFilter() {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String origin = request.getHeader("Origin");
            String referer = request.getHeader("Referer");
            String cookieHeader = request.getHeader("Cookie");

            log.debug("Incoming request: method={} uri={} origin={} referer={} cookies={}",
                    method, uri, origin, referer, cookieHeader);

            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    log.debug("Cookie present: name={} value={}", c.getName(), c.getValue());
                }
            } else {
                log.debug("No cookies present on request");
            }

            
            // Log CSRF attribute if present
            Object csrfAttr = request.getAttribute("_csrf");
            if (csrfAttr != null) {
                log.debug("_csrf attribute present: {}", csrfAttr);
            } else {
                log.debug("_csrf attribute not present");
            }

            if (method.matches("POST|PUT|DELETE")) {
                boolean originInvalid = origin != null && !origin.equals("http://localhost:8100");
                boolean refererInvalid = referer != null && !referer.startsWith("http://localhost:8100");

                if (originInvalid || refererInvalid) {
                    log.warn("Rejecting request due to origin/referer check: origin={} referer={}", origin, referer);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid origin");
                    return;
                } else {
                    log.debug("Origin/referer check passed (or headers missing but allowed).");
                }
            }

            filterChain.doFilter(request, response);
        }
    };
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