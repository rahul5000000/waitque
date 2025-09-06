package com.rrsgroup.user.web;

import com.rrsgroup.common.dto.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/api/users/me")
    public UserDto getLoggedInUser(@AuthenticationPrincipal UserDto user) {
        return user;
    }
}
