package com.vinaacademy.platform.feature.user.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinaacademy.platform.feature.common.constant.AppConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  @Value("${application.jwt.accessToken.expiration:3600}")
  private int accessTokenExpirationTime;

  @Value("${application.jwt.refreshToken.expiration:86400}")
  private int refreshTokenExpirationTime;

  @Value("${application.url.backend:http://localhost:8080}")
  private String issuer;

  public String generateAccessToken(UserDetails userDetails) {
    return jwtEncoder
        .encode(JwtEncoderParameters.from(createClaims(userDetails, accessTokenExpirationTime)))
        .getTokenValue();
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return jwtEncoder
        .encode(JwtEncoderParameters.from(createClaims(userDetails, refreshTokenExpirationTime)))
        .getTokenValue();
  }

  public LocalDateTime getExpirationTime(String token) {
    return LocalDateTime.ofInstant(
        (Instant) extractClaims(token).get("exp"), ZoneId.of(AppConstants.TIME_ZONE));
  }

  public String extractUsername(String token) {
    return (String) extractClaims(token).get("sub");
  }

  public String extractUserId(String token) {
    return (String) extractClaims(token).get("userId");
  }

  public String extractEmail(String token) {
    return (String) extractClaims(token).get("email");
  }

  public String extractRoles(String token) {
    Object rolesObj = extractClaims(token).get("roles");
    List<String> rolesList = new ObjectMapper().convertValue(rolesObj, new TypeReference<>() {});
    return String.join(",", rolesList);
  }

  private Map<String, Object> extractClaims(String token) {
    return jwtDecoder.decode(token).getClaims();
  }

  private JwtClaimsSet createClaims(UserDetails userDetails, int expiredTime) {
    if (userDetails == null) {
      throw new IllegalArgumentException("UserDetails cannot be null");
    }
    if (expiredTime <= 0) {
      throw new IllegalArgumentException("Expiration time must be greater than zero");
    }

    if (!(userDetails instanceof User user)) {
      throw new IllegalArgumentException("UserDetails must be an instance of User");
    }

    // Build authorities from user roles
    String[] authorities = user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toArray(String[]::new);

    JwtClaimsSet.Builder claimsSet =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(user.getEmail())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(expiredTime))
            .claim("sub", user.getEmail())
            .claim("userId", String.valueOf(user.getId()))
            .claim("email", user.getEmail())
            .claim("avatarUrl", user.getAvatarUrl() == null ? "" : user.getAvatarUrl())
            .claim("fullName", user.getFullName() == null ? "" : user.getFullName())
            .claim("roles", authorities)
            .claim("scope", authorities);

    return claimsSet.build();
  }

  public boolean isValidToken(String token) {
    if (StringUtils.isBlank(token)) {
      return false;
    }

    try {
      jwtDecoder.decode(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String extractAvatarUrl(String token) {
    return (String) extractClaims(token).get("avatarUrl");
  }

  public String extractFullName(String token) {
    return (String) extractClaims(token).get("fullName");
  }
}
