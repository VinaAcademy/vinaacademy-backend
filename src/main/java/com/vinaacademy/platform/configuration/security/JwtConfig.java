package com.vinaacademy.platform.configuration.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.vinaacademy.platform.feature.common.utils.KeyUtils;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Creates a JwtDecoder backed by the RSA public key from the provided KeyPair.
     *
     * <p>Returns a NimbusJwtDecoder configured to validate JWTs using the KeyPair's RSA public key.
     *
     * @param keyPair the KeyPair whose public key (must be an RSAPublicKey) will be used to verify JWT signatures
     * @return a JwtDecoder that verifies JWT signatures with the provided RSA public key
     */
    @Bean
    JwtDecoder jwtDecoder(KeyPair keyPair) {
        return NimbusJwtDecoder
                .withPublicKey((RSAPublicKey) keyPair.getPublic())
                .build();
    }

    /**
     * Loads an existing RSA KeyPair from persistent storage or generates and persists a new one.
     *
     * @return the KeyPair used for JWT signing and verification
     * @throws Exception if the key pair cannot be loaded or created (I/O or generation failure)
     */
    @Bean
    KeyPair keyPair() throws Exception {
        return KeyUtils.loadOrCreateKeyPair();
    }

    /**
     * Creates a Nimbus RSA JWK (RSAKey) from the provided KeyPair for JWT encoding/decoding.
     *
     * The returned RSAKey contains the public key, the corresponding private key (for signing),
     * and a fixed key ID of "rsa-key".
     *
     * @param keyPair the KeyPair whose public and private RSA keys are used to build the JWK
     * @return an RSAKey containing the public and private RSA key material and key ID
     */
    @Bean
    RSAKey rsaKey(KeyPair keyPair) {
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID("rsa-key")
                .build();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        return (jwkSelector, securityContext) ->
                jwkSelector.select(new JWKSet(rsaKey));
    }
}
