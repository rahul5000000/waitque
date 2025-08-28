package com.rrsgroup.waitque.service;

import com.rrsgroup.waitque.domain.UserRole;
import com.rrsgroup.waitque.dto.AdminUserDto;
import com.rrsgroup.waitque.dto.SuperUserDto;
import com.rrsgroup.waitque.dto.UserDto;
import com.rrsgroup.waitque.exception.RoleNotFoundException;
import com.rrsgroup.waitque.util.JwtWrapper;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    public UserDto mapJwtToUser(Jwt jwt) throws RoleNotFoundException {
        JwtWrapper jwtWrapper = new JwtWrapper(jwt);
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

        return user;
    }
}
