package com.rrsgroup.waitque.web;

import com.rrsgroup.waitque.domain.UserRole;
import com.rrsgroup.waitque.dto.UserDto;
import com.rrsgroup.waitque.util.JwtWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class UserController {
    @GetMapping("/api/users/me")
    public UserDto getLoggedInUser(@AuthenticationPrincipal Jwt jwt) {
        JwtWrapper jwtWrapper = new JwtWrapper(jwt);
        Optional<UserRole> userRole = jwtWrapper.getUserRole();

        if(userRole.isEmpty()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have allowed UserRole");

        UserDto user = new UserDto(
                jwtWrapper.getFirstName(),
                jwtWrapper.getLastName(),
                jwtWrapper.getEmail(),
                jwtWrapper.getUsername(),
                userRole.get());

        return user;
    }
}
