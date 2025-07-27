package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * 合同任务实体类
 */
@Data
public class ContractTask {
    
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
     * 开始月份
     */
    private String start_month;
    
    /**
     * 结束月份
     */
    private String end_month;
    
    /**
     * 销售（单位：万）
     */
    private String sale_amount;
    
    /**
     * 回笼（单位：万）
     */
    private String return_amount;
} 