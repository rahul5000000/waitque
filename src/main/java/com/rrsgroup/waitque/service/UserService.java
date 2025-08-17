package com.rrsgroup.waitque.service;

import com.rrsgroup.waitque.domain.UserRole;
import com.rrsgroup.waitque.dto.UserDto;
import com.rrsgroup.waitque.exception.RoleNotFoundException;
import com.rrsgroup.waitque.util.JwtWrapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    public UserDto mapJwtToUser(Jwt jwt) throws RoleNotFoundException {
        JwtWrapper jwtWrapper = new JwtWrapper(jwt);
        Optional<UserRole> userRole = jwtWrapper.getUserRole();

        if(userRole.isEmpty()) throw new RoleNotFoundException("User does not have allowed UserRole");

        UserDto user = new UserDto(
                jwtWrapper.getFirstName(),
                jwtWrapper.getLastName(),
                jwtWrapper.getEmail(),
                jwtWrapper.getUsername(),
                userRole.get());

        return user;
    }
}
