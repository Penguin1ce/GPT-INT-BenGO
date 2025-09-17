package com.firefly.ragdemo.DTO;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken; // 可选
}
