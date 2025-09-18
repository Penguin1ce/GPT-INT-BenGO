package com.firefly.ragdemo.service.impl;

import com.firefly.ragdemo.DTO.ChatRequest;
import com.firefly.ragdemo.VO.ChatResponseVO;
import com.firefly.ragdemo.service.ChatService;
import com.firefly.ragdemo.service.RagRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final OpenAiChatModel chatModel;
    private final RagRetrievalService ragRetrievalService;

    @Override
    public ChatResponseVO chat(ChatRequest request, String userId) {
        try {
            String finalPrompt = buildSystemPrompt(request) + "\n\n" + buildRagContext(request, userId) + "\n\n" + buildConversationPrompt(request.getMessages());

            var response = chatModel.call(finalPrompt);
            String content = response;

            ChatResponseVO.UsageVO usageVO = ChatResponseVO.UsageVO.builder()
                    .promptTokens(estimateTokens(finalPrompt))
                    .completionTokens(estimateTokens(content))
                    .totalTokens(estimateTokens(finalPrompt) + estimateTokens(content))
                    .build();

            return ChatResponseVO.builder()
                    .response(content)
                    .usage(usageVO)
                    .build();

        } catch (Exception e) {
            log.error("Chat request failed for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("对话请求失败: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> chatStream(ChatRequest request, String userId) {
        try {
            String finalPrompt = buildSystemPrompt(request) + "\n\n" + buildRagContext(request, userId) + "\n\n" + buildConversationPrompt(request.getMessages());
            return chatModel.stream(finalPrompt)
                    .map(chunk -> chunk != null ? chunk : "")
                    .filter(content -> !content.isEmpty());
        } catch (Exception e) {
            log.error("Stream chat request failed for user {}: {}", userId, e.getMessage(), e);
            return Flux.error(new RuntimeException("流式对话请求失败: " + e.getMessage()));
        }
    }

    private int estimateTokens(String text) {
        return text != null ? (text.length() / 4) : 0;
    }

    private String buildSystemPrompt(ChatRequest request) {
        String langHint = request.getLangid();
        String languageDirective = "";
        if (langHint != null && !langHint.isBlank()) {
            languageDirective = "\n- 回答语言：优先使用简体中文；如用户指定 '" + langHint + "' 或上下文明确要求其它语言，则遵循用户偏好。";
        } else {
            languageDirective = "\n- 回答语言：使用简体中文。";
        }

        return "你现在是一名重庆大学大数据与软件学院 C++ 课程的助教（Teaching Assistant，简称TA）。"
                + "你的目标是帮助学生理解与掌握 C++ 编程（默认 C++17 标准），并提供清晰、正确、可运行的示例。\n\n"
                + "请遵循以下规则：\n"
                + "- 角色定位：重庆大学大数据与软件学院 C++ 助教。\n"
                + "- 专业性：解释要准确，必要时给出时间/空间复杂度与边界条件。\n"
                + "- 示例代码：默认使用 C++17，包含必要的头文件与 main 函数或可直接调用的片段。\n"
                + "- 结构化表达：先给出结论，再给步骤/要点；必要时给简短示例。\n"
                + "- 安全与诚信：不编造不存在的库/接口；不确定时请先澄清需求或说明限制。\n"
                + languageDirective
                + "\n- 交互方式：若问题含糊，请用 1-2 句澄清提问再继续。";
    }

    private String buildRagContext(ChatRequest request, String userId) {
        try {
            // 取用户最后一条消息作为查询意图
            List<ChatRequest.ChatMessage> messages = request.getMessages();
            if (messages == null || messages.isEmpty()) return "";
            String lastUser = null;
            for (int i = messages.size() - 1; i >= 0; i--) {
                if ("user".equalsIgnoreCase(messages.get(i).getRole())) {
                    lastUser = messages.get(i).getContent();
                    break;
                }
            }
            if (lastUser == null || lastUser.isBlank()) return "";
            List<String> contexts = ragRetrievalService.retrieveContext(userId, lastUser, 5, 50);
            if (contexts.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            sb.append("[知识库检索结果，仅作参考，请结合对话与题意作答]\n");
            for (int i = 0; i < contexts.size(); i++) {
                sb.append("# 片段").append(i + 1).append("\n").append(contexts.get(i)).append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("RAG上下文构建失败，退化为普通对话: {}", e.getMessage());
            return "";
        }
    }

    private String buildConversationPrompt(List<ChatRequest.ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        int fromIndex = Math.max(0, messages.size() - 20);
        StringBuilder sb = new StringBuilder();
        sb.append("对话历史（按时间顺序）：\n");
        for (int i = fromIndex; i < messages.size(); i++) {
            ChatRequest.ChatMessage msg = messages.get(i);
            String role = msg.getRole();
            String content = msg.getContent();
            if (role == null) role = "user";
            sb.append("- ").append(role).append(": ").append(content).append("\n");
        }
        sb.append("\n请在理解上述上下文的基础上，回答最后一条用户消息。");
        return sb.toString();
    }
} 