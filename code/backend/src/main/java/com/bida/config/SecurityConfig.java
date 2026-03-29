package com.bida.config;

import com.bida.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for Quản Lý Quán Bida.
 *
 * Access rules:
 *  - /login, static resources, WebSocket → public
 *  - /admin/**                            → ADMIN only
 *  - /dashboard/**, /api/**               → ADMIN or STAFF
 *  - everything else                      → authenticated
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // ------------------------------------------------------------------ //
    //  Security Filter Chain                                               //
    // ------------------------------------------------------------------ //

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
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
                // WebSocket endpoint (also excluded from CSRF below)
                .requestMatchers("/ws/**").permitAll()
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
            // Disable for WebSocket handshake and stateless API calls
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ws/**", "/api/**")
            )

            // ---- UserDetailsService ----
            .userDetailsService(userDetailsService);

        return http.build();
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
