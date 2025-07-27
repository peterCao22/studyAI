package com.seeyon.A8ContractPost.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录响应模型
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends ApiResponse {
    
    /**
     * 用户信息
     */
    private String user;
} 