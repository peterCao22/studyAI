package com.marcopolo.hima01.config;


import com.marcopolo.hima01.tools.CourseTools;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 与AI有关的配置类
@Configuration
public class CommonConfiguration {

    /*
    *  会话记忆,将消息窗口保持到指定的最大大小
    * */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /*
    * 聊天chat模型
    * */
    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultSystem("你是一个热心、可爱的智能助手，你的名字叫小团团，请以小团团的身份和语气回答问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * 游戏聊天模型
     * @param model
     * @param chatMemory
     * @return
     */
    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }

    /**
     * 工具聊天模型,通过 defaultTools定义工具service
     */

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, ChatMemory chatMemory, CourseTools courseTools) {
        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.CUSTOMER_SERVICE_SYSTEM)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .defaultTools(courseTools)
                .build();
    }


    @Bean
    public ChatClient pdfChatClient(
            OpenAiChatModel model,
            ChatMemory chatMemory,
            VectorStore vectorStore) {
        return ChatClient.builder(model)
                .defaultSystem("请根据提供的上下文回答问题，不要自己猜测。")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory), // CHAT MEMORY
                        new SimpleLoggerAdvisor(),
                        new QuestionAnswerAdvisor(
                                vectorStore, // 向量库
                                SearchRequest.builder() // 向量检索的请求参数
                                        .similarityThreshold(0.5d) // 相似度阈值
                                        .topK(2) // 返回的文档片段数量
                                        .build()
                        )
                )
                .build();
    }

}
