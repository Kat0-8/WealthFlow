package org.example.wealthflow.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.wealthflow.configs.JwtTokenConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {
    private final JwtTokenConfig jwtTokenConfig;
    private Key key;
    private long expirationSeconds;
    private long clockSkewSeconds;

    public JwtTokenService(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }

    @PostConstruct
    public void init() {
        String secret = jwtTokenConfig.getSecret();
        if(secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes();
        }

        if(keyBytes.length < 32) {
            throw new IllegalStateException("JWT key length is too short; provide at least 32 bytes");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = jwtTokenConfig.getAccessTokenExpirationSes();
        this.clockSkewSeconds = jwtTokenConfig.getClockSkewSec();
    }

    public String generateToken(Long userId, String role) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expireAt = Date.from(now.plusSeconds(expirationSeconds));

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(issuedAt)
                .setExpiration(expireAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .build()
                .parseClaimsJws(token);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Jws<Claims> jws = parseClaims(token);
        String sub = jws.getBody().getSubject();
        return Long.valueOf(sub);
    }

    public Authentication getAuthentication(String token) {
        Jws<Claims> jws = parseClaims(token);
        Claims claims = jws.getBody();
        String role = claims.get("role", String.class);
        Long userId = Long.valueOf(claims.getSubject());
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role));
        return new UsernamePasswordAuthenticationToken(userId, token, List.of(authority));
    }
}
