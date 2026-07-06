package com.billsplit.dto;
import jakarta.validation.constraints.*;

public class AuthDtos {
    public record RegisterRequest(@NotBlank @Email String email,
                                   @NotBlank String password,
                                   @NotBlank String fullName) {}
    public record LoginRequest(@NotBlank @Email String email,
                                @NotBlank String password) {}
    public record AuthResponse(String token, Long userId,
                                String fullName, String email) {}
}
