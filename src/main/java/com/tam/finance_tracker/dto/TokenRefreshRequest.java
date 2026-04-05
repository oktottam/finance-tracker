package com.tam.finance_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh Token không được để trống đâu Tâm nhé!")
    private String refreshToken;
}
