package com.retrievo.controller;

import com.retrievo.dto.*;
import com.retrievo.entity.User;
import com.retrievo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> res = new HashMap<>();
        res.put("message", "Logged out successfully.");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> res = new HashMap<>();
        res.put("user", new UserDTO(user));
        return ResponseEntity.ok(res);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body
    ) {
        UserDTO updated = userService.updateUserProfile(user, body.get("fullName"), body.get("phone"));
        Map<String, Object> res = new HashMap<>();
        res.put("user", updated);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String token = userService.createPasswordResetToken(body.get("email"));
        Map<String, String> res = new HashMap<>();
        res.put("message", "Password reset instructions processed.");
        if (token != null) {
            res.put("resetToken", token);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        userService.resetPassword(body.get("token"), body.get("newPassword"));
        Map<String, String> res = new HashMap<>();
        res.put("message", "Password has been reset successfully.");
        return ResponseEntity.ok(res);
    }
}
