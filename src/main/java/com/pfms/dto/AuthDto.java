package com.pfms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Objects for Authentication operations.
 */
public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank @Email(message = "Valid email is required")
        private String email;

        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String fullName;
        private String email;
        private String currency;

        public JwtResponse(String token, Long id, String fullName, String email, String currency) {
            this.token = token;
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.currency = currency;
        }
    }

    @Data
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 6) private String newPassword;
    }

    @Data
    public static class UpdateProfileRequest {
        private String fullName;
        private String currency;
    }
}
