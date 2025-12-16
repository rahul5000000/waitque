package com.rrsgroup.common.security;

import com.rrsgroup.common.domain.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.rrsgroup.common.domain.UserRole.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        if ("dev".equals(activeProfile) || "docker".equals(activeProfile) || "ecs-staging".equals(activeProfile)) {
            config.setAllowedOrigins(List.of("http://localhost:8081"));
        } else {
            config.setAllowedOrigins(List.of("https://waitque.com"));
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
