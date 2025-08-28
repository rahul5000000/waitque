package com.rrsgroup.waitque.security;

import static com.rrsgroup.waitque.domain.UserRole.SUPERUSER;
import static com.rrsgroup.waitque.domain.UserRole.ADMIN;

import com.rrsgroup.waitque.domain.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .requestMatchers("/api/internal/**").hasRole(SUPERUSER.getRoleName())
                .requestMatchers("/api/admin/**").hasRole(ADMIN.getRoleName())
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationPrincipalConverter()))
            );
        return http.build();
    }
}
