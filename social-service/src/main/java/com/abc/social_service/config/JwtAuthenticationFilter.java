package com.abc.social_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Key jwtSecretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.debug("JWT Filter - Request URI: {}, Method: {}", requestURI, method);
        
        // Skip JWT validation for public endpoints
        if (requestURI.startsWith("/actuator/") || 
            requestURI.startsWith("/v3/api-docs") ||
            requestURI.startsWith("/swagger-ui")) {
            log.debug("JWT Filter - Skipping JWT validation for public endpoint");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Allow GET requests without authentication (read-only)
        if ("GET".equals(method)) {
            log.debug("JWT Filter - Skipping JWT validation for GET request");
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("JWT Filter - No valid Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            Object userIdClaim = claims.get("userId");
            
            // Get userId from claims
            Long userId = null;
            if (userIdClaim != null) {
                if (userIdClaim instanceof Long) {
                    userId = (Long) userIdClaim;
                } else if (userIdClaim instanceof Integer) {
                    userId = ((Integer) userIdClaim).longValue();
                } else if (userIdClaim instanceof String) {
                    try {
                        userId = Long.parseLong((String) userIdClaim);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse userId from token: {}", userIdClaim);
                    }
                }
            }
            
            // If userId not found in claims, try to parse from subject
            if (userId == null && subject != null) {
                try {
                    userId = Long.parseLong(subject);
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse userId from subject: {}", subject);
                }
            }

            if (userId != null && roles != null) {
                log.info("[JWT] Processing token for userId: {}, roles: {}", userId, roles);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                log.info("[JWT] Created authorities: {}", authorities);

                // Create JWT object for AuthenticationUtil
                Map<String, Object> headers = new HashMap<>();
                headers.put("alg", "HS256");
                headers.put("typ", "JWT");
                
                Map<String, Object> jwtClaims = new HashMap<>();
                jwtClaims.put("userId", userId);
                jwtClaims.put("user_id", userId);
                jwtClaims.put(JwtClaimNames.SUB, subject != null ? subject : userId.toString());
                jwtClaims.put("roles", roles);
                jwtClaims.put(JwtClaimNames.IAT, claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : Instant.now());
                jwtClaims.put(JwtClaimNames.EXP, claims.getExpiration() != null ? claims.getExpiration().toInstant() : Instant.now().plusSeconds(3600));
                
                Jwt jwt = new Jwt(token, 
                    claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : Instant.now(),
                    claims.getExpiration() != null ? claims.getExpiration().toInstant() : Instant.now().plusSeconds(3600),
                    headers,
                    jwtClaims);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        jwt, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT Filter - Authentication set for user: {} with roles: {}", userId, authorities);
            } else {
                log.warn("JWT Filter - userId or roles not found in token");
            }

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
