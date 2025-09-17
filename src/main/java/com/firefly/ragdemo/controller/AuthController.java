package com.firefly.ragdemo.controller;

import com.firefly.ragdemo.DTO.LoginRequest;
import com.firefly.ragdemo.DTO.LogoutRequest;
import com.firefly.ragdemo.DTO.RefreshTokenRequest;
import com.firefly.ragdemo.DTO.RegisterRequest;
import com.firefly.ragdemo.VO.ApiResponse;
import com.firefly.ragdemo.VO.LoginResponseVO;
import com.firefly.ragdemo.VO.UserVO;
import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.secutiry.CustomUserPrincipal;
import com.firefly.ragdemo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserVO>> register(@Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<ApiResponse.ValidationError> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> ApiResponse.ValidationError.builder()
                            .field(error.getField())
                            .message(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());

            ApiResponse<UserVO> response = ApiResponse.error("参数验证失败", 400, errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ApiResponse<UserVO> response = authService.register(request);

            if (response.getSuccess()) {
                return ResponseEntity.status(201).body(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }

        } catch (Exception e) {
            log.error("用户注册失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器内部错误"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseVO>> login(@Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<ApiResponse.ValidationError> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> ApiResponse.ValidationError.builder()
                            .field(error.getField())
                            .message(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());

            ApiResponse<LoginResponseVO> response = ApiResponse.error("参数验证失败", 400, errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ApiResponse<LoginResponseVO> response = authService.login(request);
            return ResponseEntity.status(response.getCode()).body(response);

        } catch (Exception e) {
            log.error("用户登录失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器内部错误"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseVO>> refresh(@Valid @RequestBody RefreshTokenRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<ApiResponse.ValidationError> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> ApiResponse.ValidationError.builder()
                            .field(error.getField())
                            .message(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());

            ApiResponse<LoginResponseVO> response = ApiResponse.error("参数验证失败", 400, errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ApiResponse<LoginResponseVO> response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.status(response.getCode()).body(response);

        } catch (Exception e) {
            log.error("Token刷新失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器内部错误"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody(required = false) LogoutRequest request) {

        try {
            String refreshToken = (request != null) ? request.getRefreshToken() : null;
            ApiResponse<Void> response = authService.logout(principal.getUserId(), refreshToken);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("用户登出失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器内部错误"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserVO>> getProfile(@AuthenticationPrincipal CustomUserPrincipal principal) {

        try {
            User user = principal.getUser();

            UserVO userVO = UserVO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .createdAt(user.getCreatedAt())
                    .lastLogin(user.getLastLogin())
                    .build();

            ApiResponse<UserVO> response = ApiResponse.success("获取用户信息成功", userVO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("服务器内部错误"));
        }
    }
}
