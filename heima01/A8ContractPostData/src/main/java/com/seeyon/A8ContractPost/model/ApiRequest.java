package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * API请求基础类
 */
@Data
public class ApiRequest {
    
    /**
     * 令牌
     */
    private String Token;
    
    /**
     * 语言
     */
    private String Langz_Code;
} 