package com.firefly.ragdemo.VO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVO {

    private String id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
