package com.bida.config;

import com.bida.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for Quản Lý Quán Bida.
 *
 * Access rules:
 *  - /login, static resources, WebSocket → public
 *  - /api/auth/**                         → public (login API for React frontend)
 *  - /admin/**                            → ADMIN only
 *  - /dashboard/**, /api/**               → ADMIN or STAFF
 *  - everything else                      → authenticated
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity                 // ← Kích hoạt @PreAuthorize trên từng method
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // ------------------------------------------------------------------ //
    //  Security Filter Chain                                               //
    // ------------------------------------------------------------------ //

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ---- CORS ----
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ---- Authorization rules ----
            .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico"
                ).permitAll()
                // Auth API for React frontend (public)
                .requestMatchers("/api/auth/**").permitAll()
                // WebSocket endpoint (also excluded from CSRF below)
                .requestMatchers("/ws/**").permitAll()
                // Specific admin endpoints allowed for staff (Invoice Print)
                .requestMatchers("/admin/invoices/*/print").hasAnyRole("ADMIN", "STAFF")
                // Admin-only area
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Shared area for both roles
                .requestMatchers("/dashboard/**", "/api/**").hasAnyRole("ADMIN", "STAFF")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // ---- Form login ----
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )

            // ---- Logout ----
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // ---- CSRF ----
            // Disable for WebSocket handshake and stateless API calls ONLY.
            // DO NOT ignore /admin/** → instead add CSRF token to Thymeleaf forms.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ws/**", "/api/**", "/login")
            )

            // ---- UserDetailsService ----
            .userDetailsService(userDetailsService);

        return http.build();
    }

    // ------------------------------------------------------------------ //
    //  CORS Configuration for React Frontend                               //
    // ------------------------------------------------------------------ //

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ------------------------------------------------------------------ //
    //  Beans                                                               //
    // ------------------------------------------------------------------ //

    /**
     * BCrypt password encoder – strength 12 for good security/performance balance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes the AuthenticationManager so other components (e.g., REST auth
     * endpoints) can inject and use it directly.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
