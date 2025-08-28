package com.rrsgroup.waitque.security;

import com.rrsgroup.waitque.domain.UserRole;
import com.rrsgroup.waitque.domain.WaitqueAuthenticationToken;
import com.rrsgroup.waitque.dto.AdminUserDto;
import com.rrsgroup.waitque.dto.SuperUserDto;
import com.rrsgroup.waitque.dto.UserDto;
import com.rrsgroup.waitque.exception.RoleNotFoundException;
import com.rrsgroup.waitque.util.JwtWrapper;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class JwtAuthenticationPrincipalConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter = new KeycloakRealmRoleConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        JwtWrapper jwtWrapper = new JwtWrapper(jwt);
        var authorities = keycloakRealmRoleConverter.convert(jwt);

        Optional<UserRole> userRole = jwtWrapper.getUserRole();

        if(userRole.isEmpty()) throw new RoleNotFoundException("User does not have allowed UserRole");

        UserDto user;
        if(userRole.get() == UserRole.SUPERUSER) {
            user = new SuperUserDto(
                    jwtWrapper.getFirstName(),
                    jwtWrapper.getLastName(),
                    jwtWrapper.getEmail(),
                    jwtWrapper.getUsername(),
                    userRole.get());
        } else if(userRole.get() == UserRole.ADMIN) {
            if(jwtWrapper.getCompanyId().isEmpty()) throw new IllegalStateException("Admin user is not configured with a company");
            user = new AdminUserDto(
                    jwtWrapper.getFirstName(),
                    jwtWrapper.getLastName(),
                    jwtWrapper.getEmail(),
                    jwtWrapper.getUsername(),
                    userRole.get(),
                    jwtWrapper.getCompanyId().get());
        } else {
            throw new NotImplementedException("User of type " + userRole.get() + " is not supported");
        }

        return new WaitqueAuthenticationToken(user, jwt, authorities);
    }
}
