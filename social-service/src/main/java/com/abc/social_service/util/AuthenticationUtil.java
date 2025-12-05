package com.abc.social_service.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for extracting authentication information from security context
 */
public class AuthenticationUtil {

    /**
     * Extract user ID from JWT token
     * @return user ID or null if not found
     */
    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            // Try to get user ID from different possible claims
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim == null) {
                userIdClaim = jwt.getClaim("user_id");
            }
            if (userIdClaim == null) {
                userIdClaim = jwt.getClaim("sub");
            }
            
            if (userIdClaim != null) {
                if (userIdClaim instanceof Long) {
                    return (Long) userIdClaim;
                } else if (userIdClaim instanceof Integer) {
                    return ((Integer) userIdClaim).longValue();
                } else if (userIdClaim instanceof String) {
                    try {
                        return Long.parseLong((String) userIdClaim);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Extract user role from authentication context
     * @return "ADMIN" if user has admin role, "USER" otherwise
     */
    public static String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "USER";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return "USER";
        }

        // Check if user has ADMIN role
        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> {
                    String authority = auth.getAuthority();
                    return authority != null && (
                            authority.equals("ROLE_ADMIN") ||
                            authority.equals("ADMIN") ||
                            authority.equals("admin")
                    );
                });

        return isAdmin ? "ADMIN" : "USER";
    }

    /**
     * Check if current user is an administrator
     * @return true if user has admin role
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getUserRole());
    }

    /**
     * Get all roles/authorities for the current user
     * @return list of role names
     */
    public static List<String> getAllRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return List.of();
        }

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
