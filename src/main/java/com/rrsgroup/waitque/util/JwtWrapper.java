package com.rrsgroup.waitque.util;

import com.rrsgroup.waitque.domain.JwtClaimKey;
import com.rrsgroup.waitque.domain.UserRole;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JwtWrapper {
    private Jwt jwt;

    public JwtWrapper(Jwt jwt) {
        this.jwt = jwt;
    }

    public String getFirstName() {
        return getStringClaim(JwtClaimKey.FIRST_NAME);
    }

    public String getLastName() {
        return getStringClaim(JwtClaimKey.LAST_NAME);
    }

    public String getEmail() {
        return getStringClaim(JwtClaimKey.EMAIL);
    }

    public String getUsername() {
        return getStringClaim(JwtClaimKey.USERNAME);
    }

    public List<String> getRealmRoles() {
        Map<String, Object> realmAccess = jwt.getClaim(JwtClaimKey.REALM_ACCESS.getClaimKey());
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList();
        }
        Object roles = realmAccess.get("roles");
        if (!(roles instanceof List<?> roleList)) {
            return Collections.emptyList();
        }
        return roleList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    public Optional<UserRole> getUserRole() {
        List<String> realmRoles = getRealmRoles();

        if(realmRoles.isEmpty()) return Optional.empty();

        return realmRoles.stream().map(UserRole::byRoleName).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    private String getStringClaim(JwtClaimKey key) {
        return jwt.getClaim(key.getClaimKey());
    }
}
