package com.firefly.ragdemo.service;

import com.firefly.ragdemo.DTO.LoginRequest;
import com.firefly.ragdemo.DTO.RegisterRequest;
import com.firefly.ragdemo.VO.ApiResponse;
import com.firefly.ragdemo.VO.LoginResponseVO;
import com.firefly.ragdemo.VO.UserVO;

public interface AuthService {

    ApiResponse<UserVO> register(RegisterRequest request);

    ApiResponse<LoginResponseVO> login(LoginRequest request);

    ApiResponse<LoginResponseVO> refreshToken(String refreshTokenValue);

    ApiResponse<Void> logout(String userId, String refreshTokenValue);
}
