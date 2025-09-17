package com.firefly.ragdemo.service;

import com.firefly.ragdemo.DTO.ChatRequest;
import com.firefly.ragdemo.VO.ChatResponseVO;
import reactor.core.publisher.Flux;

public interface ChatService {

    ChatResponseVO chat(ChatRequest request, String userId);

    Flux<String> chatStream(ChatRequest request, String userId);
}
