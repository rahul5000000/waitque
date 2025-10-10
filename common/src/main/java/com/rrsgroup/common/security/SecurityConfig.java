package com.rrsgroup.common.security;

import com.rrsgroup.common.domain.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.rrsgroup.common.domain.UserRole.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/api/public/**").permitAll()
                .requestMatchers("/api/users/**").hasAnyRole(UserRole.getAllRoleNames())
                .requestMatchers("/api/system/**").hasRole(SYSTEM.getRoleName())
                .requestMatchers("/api/internal/**").hasRole(SUPERUSER.getRoleName())
                .requestMatchers("/api/admin/**").hasRole(ADMIN.getRoleName())
                .requestMatchers("/api/field/**").hasRole(FIELD_USER.getRoleName())
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationPrincipalConverter()))
            );
        return http.build();
    }
}
