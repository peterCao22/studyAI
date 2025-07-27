package com.seeyon.A8ContractPost.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合同创建响应类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractCreateResponse extends ApiResponse {
    
    /**
     * 合同号
     */
    private String order_no;
} 