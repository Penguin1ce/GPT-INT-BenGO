package com.firefly.ragdemo.service;

import com.firefly.ragdemo.DTO.ChatRequest;
import com.firefly.ragdemo.VO.ChatResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final OpenAiChatModel chatModel;

    public ChatResponseVO chat(ChatRequest request, String userId) {
        try {
            // 简化消息构建：使用系统提示词 + 用户最后一条消息
            String lastMessage = request.getMessages().get(request.getMessages().size() - 1).getContent();
            String finalPrompt = buildSystemPrompt(request) + "\n\n用户消息：" + lastMessage;

            var response = chatModel.call(finalPrompt);

            String content = response;

            // 创建使用统计（模拟）
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

    public Flux<String> chatStream(ChatRequest request, String userId) {
        try {
            // 流式：使用系统提示词 + 用户最后一条消息
            String lastMessage = request.getMessages().get(request.getMessages().size() - 1).getContent();
            String finalPrompt = buildSystemPrompt(request) + "\n\n用户消息：" + lastMessage;

            return chatModel.stream(finalPrompt)
                    .map(chunk -> chunk != null ? chunk : "")
                    .filter(content -> !content.isEmpty());

        } catch (Exception e) {
            log.error("Stream chat request failed for user {}: {}", userId, e.getMessage(), e);
            return Flux.error(new RuntimeException("流式对话请求失败: " + e.getMessage()));
        }
    }

    private int estimateTokens(String text) {
        // 简单的token估算：大约每4个字符为1个token
        return text != null ? (text.length() / 4) : 0;
    }

    private String buildSystemPrompt(ChatRequest request) {
        String langHint = request.getLangid();
        String languageDirective = "";
        if (langHint != null && !langHint.isBlank()) {
            // 提示语言偏好（默认中文），仅作软约束
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
}
