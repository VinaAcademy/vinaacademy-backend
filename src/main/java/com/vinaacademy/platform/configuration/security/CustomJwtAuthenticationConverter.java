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

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> scopes = jwt.getClaimAsStringList("scope");
    if (scopes != null && !scopes.isEmpty()) {
      return scopes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    return Collections.emptyList();
  }
}
