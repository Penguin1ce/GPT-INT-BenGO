package com.firefly.ragdemo.VO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseVO {

    private String response;
    private UsageVO usage;

    @Data
    @Builder
    public static class UsageVO {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
