package com.vinaacademy.platform.configuration.security;

import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configures application security with multiple filter chains for different API endpoints.
 *
 * <p>This class defines several SecurityFilterChain beans:
 * <ul>
 *   <li><b>adminSecurity</b>: Secures admin APIs (e.g., endpoints starting with "/api/v1/admin/**") and allows only users with the ADMIN role.</li>
 *   <li><b>publicSecurity</b>: Secures authentication and public APIs (e.g., endpoints like "/api/v1/auth/**", "/api/v1/public/**", and API docs endpoints)
 *       by permitting all requests without authentication.</li>
 *   <li><b>userSecurity</b>: Secures user APIs (e.g., endpoints starting with "/api/v1/**") by requiring authentication.</li>
 *   <li><b>defaultSecurity</b>: Applies a catch-all policy to deny access to any request not matched by the other chains.</li>
 * </ul>
 *
 * <p>The common security configuration, applied to all filter chains via the {@code commonSecurityConfig()} method,
 * includes the following settings:
 * <ul>
 *   <li>Stateless session management (i.e., sessions are not maintained on the server).</li>
 *   <li>Disabling of CSRF protection and HTTP Basic authentication, appropriate for token-based stateless APIs.</li>
 *   <li>Enabling JWT authentication using OAuth2 resource server capabilities.</li>
 *   <li>Configuring CORS settings using a provided {@code UrlBasedCorsConfigurationSource}.</li>
 *   <li>Custom exception handling with designated {@code CustomAccessDeniedHandler} and {@code CustomAuthenticationEntryPoint}
 *       to handle access violations and authentication errors.</li>
 * </ul>
 *
 * <p>Additionally, the class exposes helper beans including:
 * <ul>
 *   <li>A {@code PasswordEncoder} bean based on BCrypt for secure password hashing.</li>
 *   <li>An {@code AuthenticationManager} bean to support authentication operations.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final UrlBasedCorsConfigurationSource corsConfigurationSource;
    @Value("${application.url.google-auth")
    private String googleAuthUrl;

    // Admin API Security
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/admin/**") // Apply to Admin API
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole(AuthConstants.ADMIN_ROLE) // Admin access only
                )
                .apply(commonSecurityConfig());

        return http.build();
    }

    // Authentication & Public API Security
    @Bean
    @Order(2)
    public SecurityFilterChain publicSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/auth/**", "/api/v1/public/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html") // Apply to Public APIs
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // No authentication required
                )
                .apply(commonSecurityConfig());

        return http.build();
    }

    // User API Security
    @Bean
    @Order(3)
    public SecurityFilterChain userSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Require authentication
                )
                .apply(commonSecurityConfig());

        return http.build();
    }

    // Default Security
    @Bean
    public SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Deny all undefined requests
                )
                .apply(commonSecurityConfig());

        return http.build();
    }

    private SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> commonSecurityConfig() {
        return new SecurityConfigurer<>() {
            @Override
            public void init(HttpSecurity http) throws Exception {
                http
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless
                        .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for APIs
                        .httpBasic(AbstractHttpConfigurer::disable) // Disable Basic Auth
                        .oauth2ResourceServer(oauth2 ->
                                oauth2.jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))) // Enable JWT authentication
                        .cors(cors -> cors.configurationSource(corsConfigurationSource))
                        .oauth2Login(oauth -> oauth
                                .authorizationEndpoint(authorization -> authorization
                                        .baseUri(googleAuthUrl) // Google Auth URL
                                )
                                .redirectionEndpoint(redirection -> redirection
                                        .baseUri("/api/v1/auth/login/oauth2/code/*") // OAuth2 redirection endpoint
                                )
                        )
                        .exceptionHandling(exceptionHandling -> exceptionHandling
                                .accessDeniedHandler(customAccessDeniedHandler) // Custom Access Denied Handler
                                .authenticationEntryPoint(customAuthenticationEntryPoint) // Custom Authentication Entry Point
                        );
            }

            @Override
            public void configure(HttpSecurity http) {
                // No additional configurations required
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
