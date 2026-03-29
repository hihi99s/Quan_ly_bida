package com.bida.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles navigation for authentication-related pages.
 *
 * <ul>
 *   <li>GET /login  → renders the login page (login.html)</li>
 *   <li>GET /       → redirects to /dashboard (authenticated users land here after root access)</li>
 * </ul>
 *
 * The actual POST /login processing is handled by Spring Security's
 * UsernamePasswordAuthenticationFilter, configured in {@link com.bida.config.SecurityConfig}.
 */
@Controller
public class LoginController {

    /**
     * Renders the custom login page.
     * If the user is already authenticated, Spring Security will redirect them away
     * from this page automatically (handled by the security filter chain).
     *
     * @return view name "login" → resolved to templates/login.html by Thymeleaf
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Root URL redirect.
     * Sends unauthenticated users through Security → /login,
     * and authenticated users directly to the dashboard.
     *
     * @return redirect instruction to /dashboard
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
