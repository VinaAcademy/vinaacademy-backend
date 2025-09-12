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
  @Bean
  AuthorizationServerSettings authorizationServerSettings(
      @Value("${application.url.backend:http://localhost:8080}") String issuer) {
    return AuthorizationServerSettings.builder()
        .issuer(issuer)
        .build();
  }
}
