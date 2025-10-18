package com.vinaacademy.platform.feature.user.auth.helpers;

import com.vinaacademy.platform.exception.UnauthorizedException;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Utility class for security-related operations.
 * Focuses on current user information retrieval from security context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityHelper {

    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user
     *
     * @return The current user
     * @throws UnauthorizedException if no user is authenticated
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof Jwt jwt) {
            username = jwt.getClaimAsString("sub");
        } else {
            username = principal.toString();
        }

        // Based on CustomUserDetailService, we're using username as the username
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    /**
     * Check if the current user has a specific role
     *
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().contains("ROLE_" + role));
    }

    /**
     * Check if the current user has any of the specified roles
     *
     * @param roles The roles to check
     * @return true if the user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current authentication object from security context
     * 
     * @return The current authentication or null if not authenticated
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
