package com.rrsgroup.waitque.web;

import com.rrsgroup.waitque.domain.UserRole;
import com.rrsgroup.waitque.dto.UserDto;
import com.rrsgroup.waitque.exception.RoleNotFoundException;
import com.rrsgroup.waitque.service.UserService;
import com.rrsgroup.waitque.util.JwtWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/me")
    public UserDto getLoggedInUser(@AuthenticationPrincipal Jwt jwt) {
        try {
            return userService.mapJwtToUser(jwt);
        } catch (RoleNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
