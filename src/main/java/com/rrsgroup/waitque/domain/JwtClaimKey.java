package com.rrsgroup.waitque.domain;

public enum JwtClaimKey {
    REALM_ACCESS("realm_access"),
    FIRST_NAME("given_name"),
    LAST_NAME("family_name"),
    EMAIL("email"),
    USERNAME("preferred_username");

    private String claimKey;

    private JwtClaimKey(String claimKey) {
        this.claimKey = claimKey;
    }

    public String getClaimKey() {
        return claimKey;
    }
}
