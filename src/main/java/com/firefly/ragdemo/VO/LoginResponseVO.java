package com.firefly.ragdemo.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseVO {

    private String token;
    private String refreshToken;
    private UserVO user;
}
