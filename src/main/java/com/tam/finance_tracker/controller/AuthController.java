package com.tam.finance_tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tam.finance_tracker.dto.JwtResponse;
import com.tam.finance_tracker.dto.LoginRequest;
import com.tam.finance_tracker.dto.TokenRefreshRequest;
import com.tam.finance_tracker.dto.TokenRefreshResponse;
import com.tam.finance_tracker.security.JwtUtils;
import com.tam.finance_tracker.service.RefreshTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Dùng AuthenticationManager để kiểm tra Username/Password từ DB
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. Nếu không có lỗi (đúng pass), đặt thông tin vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo Token "xịn" trả về cho Tâm
        String jwt = jwtUtils.generateToken(authentication.getName());
        
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
            // Thay :: bằng lambda để trình biên dịch hết bối rối
            .map(token -> refreshTokenService.verifyExpiration(token)) 
            .map(token -> {
                String newAccessToken = jwtUtils.generateToken(token.getUsername());
                return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, token.getToken()));
            })
            .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại hoặc đã hết hạn!"));
    }
}
