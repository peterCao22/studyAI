package com.marcopolo.hima01.config;

import com.marcopolo.hima01.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrokModelConfig {

    @Value("${GROK_API_KEY}")
    private String apiKey;

    @Value("${spring.ai.grok.base-url}")
    private String baseUrl;

    @Value("${spring.ai.grok.options.model}")
    private String modelName;

    @Bean(name = "grokChatClient")
    public ChatClient grokChatClient(ChatMemory chatMemory,CourseTools courseTools) {
        // 创建OpenAiApi实例
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        // 创建OpenAiChatOptions，使用GPT-4o-mini-search-preview模型
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(modelName)
                .build();

        // 创建搜索模型
        OpenAiChatModel model = new OpenAiChatModel(openAiApi, openAiChatOptions);

        // 创建并返回ChatClient，使用特殊的系统提示
        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.CUSTOMER_SERVICE_SYSTEM)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory))
                .defaultTools(courseTools)
                .build();
    }

}
