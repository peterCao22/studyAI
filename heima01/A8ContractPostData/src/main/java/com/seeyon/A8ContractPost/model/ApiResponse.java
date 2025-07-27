package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * API响应基础模型
 */
@Data
public class ApiResponse {
    
    /**
     * 响应状态码
     * 1: 成功
     * 其他: 失败
     */
    private Integer state;
    
    /**
     * 响应消息
     */
    private String mess;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return state != null && state == 1;
    }
} 