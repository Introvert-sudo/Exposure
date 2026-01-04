package com.exposure.DTOs.Auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    public Long token;
    public String message;

    public AuthResponse(Long token, String message) {
        this.token = token;
        this.message = message;
    }
}
