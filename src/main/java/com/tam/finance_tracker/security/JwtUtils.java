package com.tam.finance_tracker.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component // Đánh dấu đây là một bean quản lý bởi Spring, Tâm có thể @Autowired vào chỗ khác để dùng
@RequiredArgsConstructor // Tự động tạo constructor cho các field final, giúp code gọn hơn
@Slf4j // Dùng để log lỗi cho chuyên nghiệp
public class JwtUtils {
    private final JwtProperties jwtProperties;

    // Tạo SecretKey từ chuỗi thô để tránh lỗi Base64 
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return Jwts.builder() // Tạo một builder để xây dựng token
                .setSubject(username) // Đặt username làm subject của token
                .setIssuedAt(new Date()) // Thời điểm tạo token
                .setExpiration(new Date((new Date()).getTime() + jwtProperties.getExpirationMs())) // Thời điểm hết hạn token
                .signWith(getSigningKey(),SignatureAlgorithm.HS256) // Ký token bằng thuật toán HS256 và secret key
                .compact(); // Trả về token đã được tạo thành chuỗi
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken); // Nếu token hợp lệ, sẽ không ném ra ngoại lệ nào và chúng ta trả về true
            return true;
        } catch (ExpiredJwtException e) { // Nếu token đã hết hạn, sẽ ném ra ExpiredJwtException
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { // Nếu token có định dạng không được hỗ trợ, sẽ ném ra UnsupportedJwtException
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) { // Nếu token rỗng hoặc null, sẽ ném ra IllegalArgumentException
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUserNameFromJwtToken(String token) {
        // return Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody().getSubject(); // Lấy username từ subject của token
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
