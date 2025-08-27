package com.auth.service.service;

import com.auth.service.client.UserClient;
import com.auth.service.dto.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final JwtService jwtService;

    public UserDto getUserById(Long id) {
        return userClient.getUserById(id);
    }

    public TokenResponse login(LoginRequest request) {
        UserDto user = userClient.login(request);
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new TokenResponse(access, refresh);
    }
    public TokenResponse register(RegisterRequest request) {
        try {
            UserDto created = userClient.create(request);  // gọi sang user-service
            String access = jwtService.generateAccessToken(created);
            String refresh = jwtService.generateRefreshToken(created);
            return new TokenResponse(access, refresh);
        } catch (FeignException.Conflict e) {
            // ví dụ user-service trả 409 nếu email trùng
            throw new RuntimeException("Email đã tồn tại");
        } catch (FeignException e) {
            throw new RuntimeException("User-service lỗi: " + e.status());
        }
    }
    public TokenResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();
        Long userId = Long.valueOf(jwtService.parse(token).getSubject());
        UserDto user = userClient.getUserById(userId);
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new TokenResponse(access, refresh);
    }
}