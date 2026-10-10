package com.retrievo.service;

import com.retrievo.config.JwtTokenProvider;
import com.retrievo.dto.AuthRequest;
import com.retrievo.dto.AuthResponse;
import com.retrievo.dto.RegisterRequest;
import com.retrievo.dto.UserDTO;
import com.retrievo.entity.PasswordResetToken;
import com.retrievo.entity.Role;
import com.retrievo.entity.User;
import com.retrievo.repository.PasswordResetTokenRepository;
import com.retrievo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with email " + request.getEmail() + " already exists.");
        }

        // Public registration always assigns USER role
        User user = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                Role.USER
        );

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser.getEmail());

        return new AuthResponse(token, new UserDTO(savedUser), "User registered successfully.");
    }

    public AuthResponse loginUser(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if ("SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("Your account has been suspended by an administrator.");
        }

        String token = tokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, new UserDTO(user), "Login successful.");
    }

    public UserDTO updateUserProfile(User user, String fullName, String phone) {
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        User updated = userRepository.save(user);
        return new UserDTO(updated);
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return null;
        }
        User user = userOptional.get();

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(user, token, LocalDateTime.now().plusHours(2));
        tokenRepository.save(resetToken);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (resetToken.isExpired() || resetToken.getUsed()) {
            throw new IllegalArgumentException("Password reset token has expired or already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
