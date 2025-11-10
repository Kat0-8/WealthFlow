package org.example.wealthflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.wealthflow.auth.services.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = "Authorization";
        String auth = request.getHeader(header);
        String prefix = "Bearer ";
        if (auth != null && auth.startsWith(prefix)) {
            String token = auth.substring(prefix.length());
            try {
                if (jwtTokenService.validateToken(token)) {
                    var authn = jwtTokenService.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authn);
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                log.debug("JWT authentication failed: {}", ex.getMessage());
                authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid or expired token", ex));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
