package com.marcopolo.hima01.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchModelConfig {
    
    @Value("${OPENAI_API_KEY}")
    private String apiKey;
    
    @Value("${spring.ai.openai.chat.base-url}")
    private String baseUrl;
    
    @Bean(name = "searchChatClient")
    public ChatClient searchChatClient(ChatMemory chatMemory) {
        // 创建OpenAiApi实例
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
        
        // 创建OpenAiChatOptions，使用GPT-4o-mini-search-preview模型
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model("gpt-4o-mini-search-preview")  // 使用搜索模型
                .build();
        
        // 创建搜索模型
        OpenAiChatModel searchModel = new OpenAiChatModel(openAiApi, openAiChatOptions);
        
        // 创建并返回ChatClient，使用特殊的系统提示
        return ChatClient.builder(searchModel)
                .defaultSystem("你是一个具有联网搜索能力的智能助手，当需要最新信息时，你将使用搜索功能获取最新数据。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }
} 