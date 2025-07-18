package com.vinaacademy.platform.feature.user.auth.service;

import com.vinaacademy.platform.feature.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    @Value("${application.jwt.accessToken.expiration:3600}")
    private int accessTokenExpirationTime;
    @Value("${application.jwt.refreshToken.expiration:86400}")
    private int refreshTokenExpirationTime;

    public String generateAccessToken(UserDetails userDetails) {
        return jwtEncoder.encode(JwtEncoderParameters.from(createClaims(userDetails, accessTokenExpirationTime)))
                .getTokenValue();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return jwtEncoder.encode(JwtEncoderParameters.from(createClaims(userDetails, refreshTokenExpirationTime)))
                .getTokenValue();
    }

    public LocalDateTime getExpirationTime(String token) {
        return LocalDateTime.ofInstant((Instant) extractClaims(token).get("exp"), ZoneId.of(AppConstants.TIME_ZONE));
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Instant expired = (Instant) extractClaims(token).get("exp");
        return expired.isBefore(Instant.now());
    }

    public String extractUsername(String token) {
        return (String) extractClaims(token).get("sub");
    }

    private Map<String, Object> extractClaims(String token) {
        return jwtDecoder.decode(token).getClaims();
    }

    public int getAccessTokenExpirationSeconds() {
        return accessTokenExpirationTime; // or whatever your configured JWT expiration time is
    }

    private JwtClaimsSet createClaims(UserDetails userDetails, int expiredTime) {
        return JwtClaimsSet.builder()
                .issuer(userDetails.getUsername())
                .subject(userDetails.getUsername())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(expiredTime))
                .claim("scope", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .build();
    }
}
