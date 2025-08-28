package com.rrsgroup.waitque.domain;

import com.rrsgroup.waitque.dto.UserDto;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public class WaitqueAuthenticationToken extends AbstractAuthenticationToken {
    private final UserDto principal;
    private final Jwt jwt;

    public WaitqueAuthenticationToken(UserDto principal, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public UserDto getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }
}
