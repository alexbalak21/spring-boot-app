package app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.Map;

public class JsonUsernamePasswordAuthFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(JsonUsernamePasswordAuthFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthFilter(String defaultFilterProcessesUrl, AuthenticationManager authManager) {
        super(defaultFilterProcessesUrl);
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        Map<String, String> creds = objectMapper.readValue(request.getInputStream(), Map.class);
        String email = creds.get("email");
        String password = creds.get("password");

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, password);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {
        // ✅ Let Spring Security handle session creation and SecurityContext
        super.successfulAuthentication(request, response, chain, authResult);

        // 🔎 Log session details
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Session created for user {} with ID {}", authResult.getName(), session.getId());
        } else {
            logger.warn("No session found after successful authentication for user {}", authResult.getName());
        }

        // ✅ Write custom JSON response
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"success\",\"user\":\"" + authResult.getName() + "\"}");
        response.getWriter().flush(); // flush but don’t close, so Set-Cookie header is preserved
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
        response.getWriter().flush();
    }
}
