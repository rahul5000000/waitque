package com.rrsgroup.waitque.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum UserRole {
    ADMIN("ADMIN"),
    SUPERUSER("SUPERUSER");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static String[] getAllRoleNames() {
        return Arrays.stream(UserRole.values()).map(UserRole::getRoleName).toArray(String[]::new);
    }

    public static Optional<UserRole> byRoleName(String roleName) {
        return Arrays.stream(UserRole.values()).filter(role -> role.getRoleName().equals(roleName)).findFirst();
    }
}
