package com.bida.controller.api;

import com.bida.entity.User;
import com.bida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * REST API - Authentication for React Frontend.
 *
 * POST /api/auth/login  → Đăng nhập (trả về session cookie)
 * POST /api/auth/logout → Đăng xuất
 * GET  /api/auth/me     → Thông tin user hiện tại
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials,
                                    HttpServletRequest request) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            String role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("ROLE_STAFF");

            User user = userRepository.findByUsername(username).orElse(null);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", user != null ? user.getId() : -1,
                    "username", username,
                    "role", role.replace("ROLE_", ""),
                    "message", "Đăng nhập thành công"
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Sai tên đăng nhập hoặc mật khẩu"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã đăng xuất"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("ROLE_STAFF");
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "id", user != null ? user.getId() : -1,
                "username", auth.getName(),
                "role", role.replace("ROLE_", "")
        ));
    }
}
