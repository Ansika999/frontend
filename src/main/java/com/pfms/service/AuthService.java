package com.pfms.service;

import com.pfms.dto.AuthDto;
import com.pfms.entity.User;
import com.pfms.repository.UserRepository;
import com.pfms.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user authentication: registration, login, profile management.
 */
@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtils jwtUtils;

    /**
     * Authenticate user and return JWT token.
     */
    public AuthDto.JwtResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthDto.JwtResponse(jwt, user.getId(), user.getFullName(),
                user.getEmail(), user.getCurrency());
    }

    /**
     * Register new user with encrypted password.
     */
    @Transactional
    public AuthDto.MessageResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        return new AuthDto.MessageResponse("User registered successfully!");
    }

    /**
     * Get current authenticated user entity.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    /**
     * Update user profile (name, currency).
     */
    @Transactional
    public User updateProfile(AuthDto.UpdateProfileRequest request) {
        User user = getCurrentUser();
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getCurrency() != null) user.setCurrency(request.getCurrency());
        return userRepository.save(user);
    }

    /**
     * Change user password after validating current password.
     */
    @Transactional
    public AuthDto.MessageResponse changePassword(AuthDto.ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new AuthDto.MessageResponse("Password changed successfully");
    }
}
