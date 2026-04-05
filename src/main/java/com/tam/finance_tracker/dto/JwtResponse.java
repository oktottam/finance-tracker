package com.tam.finance_tracker.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data 
@RequiredArgsConstructor
public class JwtResponse {
    private final String token;
}
