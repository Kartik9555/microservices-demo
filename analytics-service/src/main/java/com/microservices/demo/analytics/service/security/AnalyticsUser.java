package com.microservices.demo.analytics.service.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static com.microservices.demo.analytics.service.Constants.NA;

@Builder
@Getter
public class AnalyticsUser implements UserDetails {

    private String username;

    @Setter
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public @Nullable String getPassword() {
        return NA;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
