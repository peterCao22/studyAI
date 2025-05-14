package com.marcopolo.hima01.controller;

import com.marcopolo.hima01.repository.ChatHistoryRepository;
import com.marcopolo.hima01.service.SearchEvaluator;
import com.marcopolo.hima01.service.SearchEvaluator.SearchEvalResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

// 聊天模型
@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
@Slf4j
public class ChatController {

    // 标准模型客户端
    private final ChatClient chatClient;
    
    // 搜索模型客户端，通过限定符注入
    @Autowired
    @Qualifier("searchChatClient")
    private ChatClient searchChatClient;
    
    private final ChatHistoryRepository chatHistoryRepository;
    private final SearchEvaluator searchEvaluator;

    /**
     * 添加会话记录
     * @param prompt 用户输入
     * @param chatId 会话ID
     * @return 流式响应
     */
    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt, @RequestParam("chatId") String chatId) {

        // 先把本次会话保存
        chatHistoryRepository.save("chat", chatId);
        
        // 智能判断是否需要搜索，并获取可能的直接结果
        SearchEvalResult evalResult = searchEvaluator.shouldSearch(prompt);
        log.info("用户问题: {}, 是否需要搜索: {}", prompt, evalResult.isNeedsSearch());

        // 如果模型已经输出了完整结果，直接返回
        if (evalResult.getSearchResult() != null) {
            log.info("直接返回模型查找结果，无需再次调用模型");
            return Flux.just(evalResult.getSearchResult());
        }

        // 根据判断选择不同的客户端
        if (evalResult.isNeedsSearch()) {
            log.info("使用搜索模型处理: {}", prompt);
            return searchChatClient.prompt()
                    .user(prompt)
                    .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                    .stream()
                    .content();
        }
        // 使用标准模型处理普通问题
        return chatClient.prompt()
                .user(prompt)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }
}
