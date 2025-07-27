package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * 合同保证金实体类
 */
@Data
public class ContractGuarantee {
    
    /**
     * 操作类型，新增:ADD
     */
    private String flag = "ADD";
    
    /**
     * ID，新增传0
     */
    private String id = "0";
    
    /**
     * 合同号
     */
    private String order_no;
    
    /**
     * 保证金单号
     */
    private String guarantee_no;
    
    /**
     * 保证金金额
     */
    private String guarantee_amount;
    
    /**
     * 保证金备注
     */
    private String guarantee_note;
} 