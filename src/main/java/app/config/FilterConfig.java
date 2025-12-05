package app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;


@Configuration
public class FilterConfig {
    @Bean
    public OncePerRequestFilter originCheckFilter(@Value("${app.security.allowed-origin}") String ORIGIN) {
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
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
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
