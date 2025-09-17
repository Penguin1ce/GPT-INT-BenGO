package com.firefly.ragdemo.controller;

import com.firefly.ragdemo.DTO.ChatRequest;
import com.firefly.ragdemo.VO.ApiResponse;
import com.firefly.ragdemo.VO.ChatResponseVO;
import com.firefly.ragdemo.secutiry.CustomUserPrincipal;
import com.firefly.ragdemo.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final ExecutorService executorService = 
        new DelegatingSecurityContextExecutorService(Executors.newCachedThreadPool());

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@Valid @RequestBody ChatRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (bindingResult.hasErrors()) {
            List<ApiResponse.ValidationError> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> ApiResponse.ValidationError.builder()
                            .field(error.getField())
                            .message(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());

            ApiResponse<ChatResponseVO> response = ApiResponse.error("参数验证失败", 400, errors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String userId = principal.getUserId();

            if (Boolean.TRUE.equals(request.getStream())) {
                // 流式响应
                return handleStreamResponse(request, userId);
            } else {
                // 非流式响应
                return handleNormalResponse(request, userId);
            }

        } catch (Exception e) {
            log.error("对话请求失败 for user {}: {}", principal.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("对话请求失败"));
        }
    }

    private ResponseEntity<SseEmitter> handleStreamResponse(ChatRequest request, String userId) {
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时，避免无限等待

        // 设置完成和超时回调
        emitter.onCompletion(() -> log.debug("SSE连接完成"));
        emitter.onTimeout(() -> {
            log.debug("SSE连接超时");
            emitter.complete();
        });
        emitter.onError(throwable -> {
            log.error("SSE连接出错", throwable);
            emitter.complete();
        });

        // 保存当前安全上下文
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        executorService.execute(() -> {
            // 在异步线程中设置安全上下文
            SecurityContextHolder.setContext(securityContext);
            try {
                Flux<String> responseStream = chatService.chatStream(request, userId);

                responseStream.subscribe(
                        chunk -> {
                            try {
                                // 发送SSE格式的数据
                                String sseData = String.format("data: {\"message\": {\"content\": \"%s\"}}",
                                        chunk.replace("\"", "\\\"").replace("\n", "\\n"));
                                emitter.send(SseEmitter.event().data(sseData));
                            } catch (Exception e) {
                                log.error("发送SSE数据失败", e);
                                emitter.complete(); // 使用complete而不是completeWithError
                            }
                        },
                        error -> {
                            log.error("流式对话出错", error);
                            try {
                                // 发送错误信息给客户端
                                String errorData = String.format("data: {\"error\": \"%s\"}", 
                                    error.getMessage().replace("\"", "\\\""));
                                emitter.send(SseEmitter.event().data(errorData));
                            } catch (Exception e) {
                                log.error("发送错误信息失败", e);
                            }
                            emitter.complete();
                        },
                        () -> {
                            log.info("流式对话完成");
                            try {
                                emitter.send(SseEmitter.event().data("data: [DONE]"));
                            } catch (Exception e) {
                                log.error("发送完成信号失败", e);
                            }
                            emitter.complete();
                        });

            } catch (Exception e) {
                log.error("启动流式对话失败", e);
                try {
                    String errorData = String.format("data: {\"error\": \"%s\"}", 
                        e.getMessage().replace("\"", "\\\""));
                    emitter.send(SseEmitter.event().data(errorData));
                } catch (Exception sendError) {
                    log.error("发送初始错误信息失败", sendError);
                }
                emitter.complete();
            } finally {
                // 清理安全上下文
                SecurityContextHolder.clearContext();
            }
        });

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("X-Accel-Buffering", "no") // 禁用Nginx缓冲
                .body(emitter);
    }

    private ResponseEntity<ApiResponse<ChatResponseVO>> handleNormalResponse(ChatRequest request, String userId) {
        try {
            ChatResponseVO response = chatService.chat(request, userId);
            ApiResponse<ChatResponseVO> apiResponse = ApiResponse.success("对话完成", response);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("对话请求处理失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("对话请求失败"));
        }
    }
}
