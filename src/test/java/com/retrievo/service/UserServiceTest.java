package com.retrievo.service;

import com.retrievo.config.JwtTokenProvider;
import com.retrievo.dto.AuthResponse;
import com.retrievo.dto.RegisterRequest;
import com.retrievo.entity.Role;
import com.retrievo.entity.User;
import com.retrievo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("Password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenProvider.generateToken("test@example.com")).thenReturn("dummyJwtToken");

        AuthResponse res = userService.registerUser(req);

        assertNotNull(res);
        assertEquals("dummyJwtToken", res.getToken());
        assertEquals("Test User", res.getUser().getFullName());
        assertEquals(Role.USER, res.getUser().getRole());
    }
}
