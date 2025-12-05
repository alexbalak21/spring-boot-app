package app.config;

import app.security.CustomUserDetailsService;
import app.security.JsonUsernamePasswordAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;    
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${app.security.allowed-origin}")
    private String ORIGIN;

    private static final String[] PUBLIC_ENDPOINTS = {
        "/", "/index.html", "/static/**", "/assets/**",
        "/*.js", "/*.css", "/*.json", "/*.png", "/*.jpg",
        "/*.jpeg", "/*.gif", "/*.svg", "/*.ico",
        "/favicon.ico", "/error",
        "/api/auth/register",
        "/api/csrf",
        "/about",
        "/demo",
        "/login",
        "/register",
        "/api/demo"
    };

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService uds) {
        this.userDetailsService = uds;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieCustomizer(cookie -> cookie
            .httpOnly(false)
            .secure(false)   // must be false on http://localhost
            .sameSite("Lax") // use "None" if frontend runs on a different port
            .path("/")
        );

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        RequestMatcher csrfIgnore = request -> {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                return false;
            }
            return "/api/csrf".equals(request.getRequestURI());
        };

        Logger logger = LoggerFactory.getLogger(JsonUsernamePasswordAuthFilter.class);
        JsonUsernamePasswordAuthFilter jsonFilter =
            new JsonUsernamePasswordAuthFilter("/api/auth/login", authManager);

        // ✅ FIX: Save SecurityContext into session
        jsonFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            HttpSession session = request.getSession(true); // force session creation

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);

            logger.info("Session created for user {} with ID {}", authentication.getName(), session.getId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"success\",\"user\":\"" + authentication.getName() + "\"}");
            response.getWriter().flush();
        });

        jsonFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
            response.getWriter().flush();
        });

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(tokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers(csrfIgnore)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authenticationProvider(authProvider())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(originCheckFilter(), CsrfFilter.class)
            .addFilterBefore(debugSessionFilter(), org.springframework.security.web.context.SecurityContextPersistenceFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(ORIGIN)); // e.g. http://localhost:5173
        configuration.setAllowCredentials(true); // critical for cookies
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Cache-Control",
            "Content-Type",
            "X-XSRF-TOKEN",
            "X-Requested-With"
        ));
        configuration.setExposedHeaders(Collections.singletonList("X-XSRF-TOKEN"));
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
                    if ((origin != null && !origin.equals(ORIGIN)) ||
                        (referer != null && !referer.startsWith(ORIGIN))) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid origin");
                        return;
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public OncePerRequestFilter debugSessionFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    System.out.println(">>> Incoming JSESSIONID: " +
                        Arrays.toString(request.getCookies()) +
                        " | Session ID: " + session.getId() +
                        " | Auth: " + SecurityContextHolder.getContext().getAuthentication());
                } else {
                    System.out.println(">>> No session found for request " + request.getRequestURI());
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}
