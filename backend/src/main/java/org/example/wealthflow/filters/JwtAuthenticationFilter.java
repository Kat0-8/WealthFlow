package org.example.wealthflow.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.wealthflow.services.JwtTokenService;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final String header = "Authorization";
    private final String prefix = "Bearer ";

    public JwtAuthenticationFilter(JwtTokenService tokenService) {
        this.jwtTokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader(header);
        if (auth != null && auth.startsWith(prefix)) {
            String token = auth.substring(prefix.length());
            try {
                if (jwtTokenService.validateToken(token)) {
                    var authn = jwtTokenService.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authn);
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
