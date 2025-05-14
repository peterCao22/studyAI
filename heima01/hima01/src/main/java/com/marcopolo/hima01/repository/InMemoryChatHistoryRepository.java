package com.marcopolo.hima01.repository;


// ChatHistoryRepository的实现类

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {


    // 使用map 存放会话历史记录,key: chatId, value: chatMessage
    private Map<String,List<String>> chatHistory = new HashMap<>();


    @Override
    public List<String> getChatIds(String type) {
        // 在ChatMemory中，会记录一个会话中的所有消息，记录方式是以conversationId为key，以List<Message>为value
        return chatHistory.getOrDefault(type,List.of());
    }

    @Override
    public void save(String type, String chatId) {
        // 如果chatHistory没有type的key信息，就以type为key进行put,value是一个List空对象
        // 如果有type，就返回value
        List<String> chatIds = chatHistory.computeIfAbsent(type, k -> new ArrayList<>());
        if(!chatIds.contains(chatId)) {
            chatIds.add(chatId);
        }
    }
}
