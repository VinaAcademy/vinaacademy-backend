package com.vinaacademy.platform.configuration.security;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UrlBasedCorsConfigurationSource corsConfigurationSource;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;
    
    @Value("${application.url.google-auth")
    private String googleAuthUrl;

  /**
   * Configures a SecurityFilterChain for the OAuth2 authorization server endpoints and enables JWT-based resource server support.
   *
   * The configuration restricts HttpSecurity to the authorization server endpoints provided by
   * OAuth2AuthorizationServerConfigurer, applies the authorization server defaults, and configures
   * the OAuth2 Resource Server to use JWT authentication for those endpoints.
   *
   * @return the built SecurityFilterChain that secures the authorization server endpoints and enables JWT processing
   */
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
    
    http
        .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        .with(authorizationServerConfigurer, Customizer.withDefaults())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    
    return http.build();
  }

  /**
   * Configures and returns the resource-server SecurityFilterChain used for application endpoints.
   *
   * <p>Sets a permissive request authorization policy (all requests permitted), enforces stateless
   * sessions, disables CSRF and HTTP Basic, and enables JWT-based OAuth2 resource-server support
   * using the injected custom JWT authentication converter. Also wires CORS using the provided
   * CorsConfigurationSource, configures OAuth2 login endpoints (authorization base URI comes from
   * {@code googleAuthUrl} and the redirection endpoint is {@code /api/v1/auth/login/oauth2/code/*}),
   * and attaches custom handlers for access-denied and authentication-entry-point events.</p>
   *
   * @return the configured SecurityFilterChain for the resource server
   */
  @Bean
  @Order(2)
  public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .oauth2ResourceServer(oauth2 ->
            oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)
            )
        )
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .oauth2Login(oauth -> oauth
            .authorizationEndpoint(authorization -> 
                authorization.baseUri(googleAuthUrl)
            )
            .redirectionEndpoint(redirection ->
                redirection.baseUri("/api/v1/auth/login/oauth2/code/*")
            )
        )
        .exceptionHandling(exceptionHandling ->
            exceptionHandling
                .accessDeniedHandler(customAccessDeniedHandler)
                .authenticationEntryPoint(customAuthenticationEntryPoint)
        );

    return http.build();
  }



    /**
     * Creates a PasswordEncoder bean that uses BCrypt for hashing passwords.
     *
     * <p>Provides a BCryptPasswordEncoder instance suitable for encoding and
     * verifying user credentials stored in the application.</p>
     *
     * @return a BCrypt-based PasswordEncoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
