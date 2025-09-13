package com.vinaacademy.platform.configuration.security;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
public class RegisteredClientConfig {
  public static final String SCOPE_READ = "api.read";
  public static final String SCOPE_WRITE = "api.write";
  private static final int ACCESS_TOKEN_VALIDITY_SECONDS = 3600;

  @Value("${security.oauth2.client-id:grpc-client}")
  private String clientId;
  @Value("${security.oauth2.client-secret:grpc-secret}")
  private String clientSecret;

  /**
   * Creates an in-memory RegisteredClientRepository containing a single OAuth2 client
   * configured for the client credentials grant.
   *
   * <p>The registered client uses the configured {@code clientId} and an encoded
   * {@code clientSecret}, authenticates with CLIENT_SECRET_BASIC, is granted the
   * scopes "api.read" and "api.write", and issues access tokens with a TTL of
   * 3600 seconds.
   *
   * <p>The provided PasswordEncoder is used to encode the client secret.
   *
   * @return a RegisteredClientRepository backed by an InMemoryRegisteredClientRepository
   */
  @Bean
  RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {
    var grpcMachineClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId(clientId)
        .clientSecret(encoder.encode(clientSecret))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope(SCOPE_READ)
        .scope(SCOPE_WRITE)
        .tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofSeconds(ACCESS_TOKEN_VALIDITY_SECONDS))
            .build())
        .build();

    return new InMemoryRegisteredClientRepository(grpcMachineClient);
  }
}
