package com.seeyon.A8ContractPost.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * 用户名
     */
    private String UserName;
    
    /**
     * 密码
     */
    private String PassWord;
    
    /**
     * 语言编码
     */
    private String LangzCode;
} 