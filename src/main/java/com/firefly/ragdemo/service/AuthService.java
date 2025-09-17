package com.firefly.ragdemo.service;

import com.firefly.ragdemo.DTO.LoginRequest;
import com.firefly.ragdemo.DTO.RegisterRequest;
import com.firefly.ragdemo.VO.ApiResponse;
import com.firefly.ragdemo.VO.LoginResponseVO;
import com.firefly.ragdemo.VO.UserVO;
import com.firefly.ragdemo.entity.RefreshToken;
import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.secutiry.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public ApiResponse<UserVO> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            return ApiResponse.error("用户名已存在", 409);
        }

        // 检查邮箱是否已存在
        if (userService.existsByEmail(request.getEmail())) {
            return ApiResponse.error("邮箱已存在", 409);
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        User savedUser = userService.save(user);

        UserVO userVO = UserVO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .build();

        return ApiResponse.success("注册成功", userVO, 201);
    }

    @Transactional
    public ApiResponse<LoginResponseVO> login(LoginRequest request) {
        // 验证用户
        Optional<User> userOpt = userService.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ApiResponse.error("用户名或密码错误", 401);
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ApiResponse.error("用户名或密码错误", 401);
        }

        if (!user.getIsActive()) {
            return ApiResponse.error("账户已被禁用", 403);
        }

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 更新最后登录时间
        userService.updateLastLogin(user.getId());

        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();

        LoginResponseVO responseVO = LoginResponseVO.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userVO)
                .build();

        return ApiResponse.success("登录成功", responseVO);
    }

    @Transactional
    public ApiResponse<LoginResponseVO> refreshToken(String refreshTokenValue) {
        if (!refreshTokenService.isValidRefreshToken(refreshTokenValue)) {
            return ApiResponse.error("刷新令牌无效或已过期", 401);
        }

        Optional<RefreshToken> tokenOpt = refreshTokenService.findValidToken(refreshTokenValue);
        if (tokenOpt.isEmpty()) {
            return ApiResponse.error("刷新令牌无效或已过期", 401);
        }

        RefreshToken refreshToken = tokenOpt.get();
        User user = refreshToken.getUser();

        // 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());

        // 生成新的刷新令牌（可选，这里选择生成新的）
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        // 撤销旧的刷新令牌
        refreshTokenService.revokeToken(refreshTokenValue);

        LoginResponseVO responseVO = LoginResponseVO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();

        return ApiResponse.success("Token刷新成功", responseVO);
    }

    @Transactional
    public ApiResponse<Void> logout(String userId, String refreshTokenValue) {
        // 撤销用户的所有刷新令牌
        refreshTokenService.revokeAllUserTokens(userId);

        // 如果提供了特定的刷新令牌，也撤销它
        if (refreshTokenValue != null && !refreshTokenValue.isEmpty()) {
            refreshTokenService.revokeToken(refreshTokenValue);
        }

        return ApiResponse.success("登出成功", null);
    }
}
