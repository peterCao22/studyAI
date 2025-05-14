package com.marcopolo.hima01.entity.vo;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

// 会话历史信息VO类
@Data
@NoArgsConstructor
public class MessageVO {

    private String role; // 对应的是 MessageType
    private String content;  // 模型返回的响应text

    // 由模型的Message来构造
    public MessageVO(Message message) {
        // getMessageType返回的是一个枚举，用switch处理
        this.role = switch (message.getMessageType()){
            case USER -> "user";
            case SYSTEM -> "system";
            case ASSISTANT -> "assistant";
            default -> "";
        };
        this.content = message.getText();
    }
}
