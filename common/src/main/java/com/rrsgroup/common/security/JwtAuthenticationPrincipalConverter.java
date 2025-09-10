package com.rrsgroup.common.security;

import com.rrsgroup.common.domain.UserRole;
import com.rrsgroup.common.domain.WaitqueAuthenticationToken;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.dto.SuperUserDto;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.exception.RoleNotFoundException;
import com.rrsgroup.common.util.JwtWrapper;
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
                    jwtWrapper.getUserId(),
                    jwtWrapper.getFirstName(),
                    jwtWrapper.getLastName(),
                    jwtWrapper.getEmail(),
                    jwtWrapper.getUsername(),
                    userRole.get());
        } else if(userRole.get() == UserRole.ADMIN) {
            if(jwtWrapper.getCompanyId().isEmpty()) throw new IllegalStateException("Admin user is not configured with a company");
            user = new AdminUserDto(
                    jwtWrapper.getUserId(),
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
