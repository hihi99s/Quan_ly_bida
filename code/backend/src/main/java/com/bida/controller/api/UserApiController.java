package com.bida.controller.api;

import com.bida.entity.User;
import com.bida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")     // ← Chỉ ADMIN mới quản lý nhân viên
public class UserApiController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // Hide passwords
        List<Map<String, Object>> users = userRepository.findAll().stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "username", u.getUsername(),
                "fullName", u.getFullName(),
                "role", u.getRole(),
                "active", u.getActive(),
                "phone", u.getPhone() != null ? u.getPhone() : "",
                "email", u.getEmail() != null ? u.getEmail() : ""
        )).toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tài khoản đã tồn tại"));
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getActive() == null) user.setActive(true);
        
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Thêm nhân viên thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userData) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy NV"));
        }

        existing.setFullName(userData.getFullName());
        existing.setRole(userData.getRole());
        existing.setActive(userData.getActive());
        existing.setPhone(userData.getPhone());
        existing.setEmail(userData.getEmail());

        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        userRepository.save(existing);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy NV"));
        }
        // Instead of hard delete, we disable them
        existing.setActive(false);
        userRepository.save(existing);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã vô hiệu hóa nhân viên"));
    }
}
