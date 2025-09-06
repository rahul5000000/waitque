package com.rrsgroup.common.util;

import com.rrsgroup.common.domain.JwtClaimKey;
import com.rrsgroup.common.domain.UserRole;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
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

    public Optional<Long> getCompanyId() {
        Long companyId = null;

        try {
            companyId = getLongClaim(JwtClaimKey.COMPANY_ID);
        } catch (Exception e) {
            log.warn("Tried to retrieve companyId from a JWT token that didn't contain one");
        }

        if(companyId != null) {
            return Optional.of(companyId);
        } else {
            return Optional.empty();
        }
    }

    private String getStringClaim(JwtClaimKey key) {
        return jwt.getClaim(key.getClaimKey());
    }

    private Long getLongClaim(JwtClaimKey key) {
        return jwt.getClaim(key.getClaimKey());
    }
}
