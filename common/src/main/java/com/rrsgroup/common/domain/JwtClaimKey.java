package com.rrsgroup.common.domain;

public enum JwtClaimKey {
    REALM_ACCESS("realm_access"),
    FIRST_NAME("given_name"),
    LAST_NAME("family_name"),
    EMAIL("email"),
    USERNAME("preferred_username"),
    COMPANY_ID("company_id"),
    USER_ID("sub");

    private final String claimKey;

    JwtClaimKey(String claimKey) {
        this.claimKey = claimKey;
    }

    public String getClaimKey() {
        return claimKey;
    }
}
