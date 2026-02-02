package com.rrsgroup.company.dto;

public enum KeycloakStatus {
    ENABLED, DISABLED;

    public boolean isEnabled() {
        return this == ENABLED;
    }
}
