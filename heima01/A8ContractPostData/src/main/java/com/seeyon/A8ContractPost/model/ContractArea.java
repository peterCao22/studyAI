package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * 合同区域实体类
 */
@Data
public class ContractArea {
    
    /**
     * 合同号
     */
    private String order_no;
    
    /**
     * 省编码
     */
    private String code_prov;
    
    /**
     * 市编码
     */
    private String code_city;
    
    /**
     * 区编码
     */
    private String code_coun;
    
    /**
     * 镇编码
     */
    private String code_town;
} 