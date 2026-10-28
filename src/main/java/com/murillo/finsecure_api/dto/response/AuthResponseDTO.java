package com.murillo.finsecure_api.dto.response;

public record AuthResponseDTO(
        String token,
        String tokenType,
        Long expiresIn
) {
    public static AuthResponseDTO of(String token, Long expiresIn) {
        return new AuthResponseDTO(token, "Bearer", expiresIn);
    }
}