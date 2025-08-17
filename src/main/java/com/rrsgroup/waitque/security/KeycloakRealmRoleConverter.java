package com.rrsgroup.waitque.security;

import com.rrsgroup.waitque.util.JwtWrapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        JwtWrapper jwtWrapper = new JwtWrapper(jwt);
        return jwtWrapper.getRealmRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
