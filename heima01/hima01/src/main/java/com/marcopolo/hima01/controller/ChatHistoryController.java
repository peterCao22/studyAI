package com.marcopolo.hima01.controller;


import com.marcopolo.hima01.entity.vo.MessageVO;
import com.marcopolo.hima01.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 历史记录
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/history")
public class ChatHistoryController {

    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;

    @GetMapping("/{type}")
    public List<String> getChatIds(@PathVariable("type") String type ){
        return chatHistoryRepository.getChatIds(type);
    }

    /**
     * 根据业务类型查询会话历史，目前是存储在 chatMemory 中
     * @return 指定会话的历史消息,用 MessageVO 来接收
     */
    @GetMapping("/{type}/{chatId}")
    public List<MessageVO> getChatIds(@PathVariable("type") String type, @PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        // 如果为空就返回空List
        if (messages == null) {
            return List.of();
        }
        // 使用map流进行转换成MessageVO, 传入Message类型给构造函数
        return messages.stream().map(MessageVO::new).toList();
    }
}
