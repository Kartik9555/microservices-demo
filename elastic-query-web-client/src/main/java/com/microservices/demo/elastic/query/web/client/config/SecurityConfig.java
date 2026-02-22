package com.microservices.demo.elastic.query.web.client.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String GROUPS_CLAIM = "groups";
    private static final String ROLE_PREFIX = "ROLE_";
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${security.logout-success-url}")
    private String logoutSuccessUrl;

    OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandle(){
        final var successHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri(logoutSuccessUrl);
        return successHandler;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests.requestMatchers("/").permitAll()
                        .anyRequest().fullyAuthenticated())
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandle()))
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(conf -> conf.userInfoEndpoint(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
            authorities.forEach(authority -> {
                if(authority instanceof OidcUserAuthority oidcUserAuthority) {
                    final var idToken = oidcUserAuthority.getIdToken();
                    log.info("Username from id token {}", idToken.getPreferredUsername());
                    final var userInfo = oidcUserAuthority.getUserInfo();
                    final var groupAuthorities = userInfo.getClaimAsStringList(GROUPS_CLAIM).stream()
                            .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group.toUpperCase()))
                            .toList();
                    grantedAuthorities.addAll(groupAuthorities);
                }
            });
            return grantedAuthorities;
        };
    }
}
