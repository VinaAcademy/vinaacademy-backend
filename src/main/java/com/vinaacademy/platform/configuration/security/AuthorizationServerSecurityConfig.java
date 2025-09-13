package com.vinaacademy.platform.configuration.security;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthorizationServerSecurityConfig {
  /**
   * Creates an AuthorizationServerSettings bean with the configured issuer URL.
   *
   * The `issuer` value is injected from the `application.url.backend` property and
   * defaults to `http://localhost:8080` when the property is not set.
   *
   * @param issuer the issuer URL to set on the AuthorizationServerSettings
   * @return an AuthorizationServerSettings instance with the given issuer
   */
  @Bean
  AuthorizationServerSettings authorizationServerSettings(
      @Value("${application.url.backend:http://localhost:8080}") String issuer) {
    return AuthorizationServerSettings.builder()
        .issuer(issuer)
        .build();
  }
}
