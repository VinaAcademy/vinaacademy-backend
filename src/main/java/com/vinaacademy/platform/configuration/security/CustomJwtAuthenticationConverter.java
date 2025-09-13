package com.vinaacademy.platform.configuration.security;

import com.vinaacademy.platform.exception.AccessDeniedException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  /**
   * Converts a Jwt into a Spring Security authentication token.
   *
   * Derives authorities from the token's `scope` claim and selects the principal
   * from the `email` claim if present, otherwise from the token subject.
   *
   * @param jwt the JWT to convert
   * @return a JwtAuthenticationToken containing the original Jwt, extracted authorities, and the determined principal
   * @throws AccessDeniedException if neither the `email` claim nor the subject is present or non-blank
   */
  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    String principalClaimValue =
        jwt.getClaimAsString("email") != null
            ? jwt.getClaimAsString("email")
            : jwt.getSubject();
    if (StringUtils.isBlank(principalClaimValue)) {
      throw AccessDeniedException.messageKey("unauthorized.access");
    }
    return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
  }

  /**
   * Extracts granted authorities from the JWT's "scope" claim.
   *
   * The "scope" claim is expected to be a list of strings; each scope is converted
   * to a SimpleGrantedAuthority. If the claim is missing or empty, an empty
   * collection is returned.
   *
   * @param jwt the JWT containing the "scope" claim
   * @return a collection of GrantedAuthority derived from the JWT scopes, or an empty collection if none
   */
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> scopes = jwt.getClaimAsStringList("scope");
    if (scopes != null && !scopes.isEmpty()) {
      return scopes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    return Collections.emptyList();
  }
}
