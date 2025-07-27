package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * 区域编码实体类
 */
@Data
public class AreaCode {
    
    /**
     * 编码
     */
    private String code;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 父编码
     */
    private String parentCode;
    
    /**
     * 级别
     * 1: 省
     * 2: 市
     * 3: 区/县
     * 4: 镇/街道
     */
    private Integer level;
} 