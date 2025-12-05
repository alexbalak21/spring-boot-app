package app.config;


import app.security.JsonUsernamePasswordAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.web.cors.CorsConfigurationSource;    
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    @Value("${app.security.allowed-origin}")
    private String ORIGIN;

    Logger logger = LoggerFactory.getLogger(JsonUsernamePasswordAuthFilter.class);


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
        "/user",
        "/register",
        "/api/demo"
    };

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager,
                                                   DaoAuthenticationProvider authProvider,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   OncePerRequestFilter originCheckFilter,
                                                   OncePerRequestFilter debugSessionFilter) throws Exception {

        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieCustomizer(cookie -> cookie.httpOnly(false).secure(false).sameSite("Lax").path("/"));

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        RequestMatcher csrfIgnore = request -> "POST".equalsIgnoreCase(request.getMethod()) && "/api/csrf".equals(request.getRequestURI());

        JsonUsernamePasswordAuthFilter jsonFilter = new JsonUsernamePasswordAuthFilter("/api/auth/login", authManager);

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.csrfTokenRepository(tokenRepository).csrfTokenRequestHandler(requestHandler).ignoringRequestMatchers(csrfIgnore))
            .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authenticationProvider(authProvider)
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(originCheckFilter, CsrfFilter.class)
            .addFilterBefore(debugSessionFilter, org.springframework.security.web.context.SecurityContextPersistenceFilter.class);

        return http.build();
    }
}
