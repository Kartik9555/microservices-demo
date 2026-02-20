package com.microservices.demo.elastic.query.service.security;

import com.microservices.demo.elastic.query.service.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class TwitterQueryUserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String SCOPE_CLAIM = "scope";
    private static final String USERNAME_CLAIM = "username";
    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_SCOPE_PREFIX = "SCOPE_";
    private static final String SCOPE_SEPARATOR = " ";

    private final TwitterQueryUserDetailsService twitterQueryUserDetailsService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authoritiesFromJwt = getAuthoritiesFromJwt(jwt);
        return Optional.of(twitterQueryUserDetailsService.loadUserByUsername(jwt.getClaimAsString(USERNAME_CLAIM)))
                .map(userDetails -> {
                    ((TwitterQueryUser)userDetails).setAuthorities(authoritiesFromJwt);
                    return new UsernamePasswordAuthenticationToken(userDetails, Constants.NA, authoritiesFromJwt);
                }).orElseThrow(() -> new BadCredentialsException("User could not be found!"));
    }

    private Collection<GrantedAuthority> getAuthoritiesFromJwt(Jwt jwt) {
        return getCombinedAuthorities(jwt).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private Collection<String> getCombinedAuthorities(Jwt jwt) {
        Collection<String> authorities = getRoles(jwt);
        authorities.addAll(getScopes(jwt));
        return authorities;
    }

    private Collection<String> getRoles(Jwt jwt) {
        final var roles = jwt.getClaims().get(ROLES_CLAIM);
        if (roles instanceof Collection) {
            return ((Collection<String>) roles).stream()
                    .map(role -> DEFAULT_ROLE_PREFIX + role.toUpperCase())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Collection<String> getScopes(Jwt jwt) {
        final var scopes = jwt.getClaims().get(SCOPE_CLAIM);
        if (scopes instanceof String) {
            return Arrays.stream(((String)scopes).split(SCOPE_SEPARATOR))
                    .map(scope -> DEFAULT_SCOPE_PREFIX + scope.toUpperCase())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
