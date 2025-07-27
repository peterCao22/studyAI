package com.seeyon.A8ContractPost.model;

import lombok.Data;

/**
 * 合同实体类
 */
@Data
public class Contract {
    
    /**
     * 操作类型，新增:ADD
     */
    private String flag = "ADD";
    
    /**
     * 合同号，新增时传空值
     */
    private String order_no = "";
    
    /**
     * 合同名称
     */
    private String contract_name;
    
    /**
     * 签约日期
     */
    private String signcontract_date;
    
    /**
     * 初审
     */
    private String first_check;
    
    /**
     * 复审
     */
    private String recheck;
    
    /**
     * 签约人
     */
    private String signcontract_man;
    
    /**
     * 性质（传名称）
     */
    private String property;
    
    /**
     * 客户编码
     */
    private String cust_no;
    
    /**
     * 合同类型（已不需要传）
     */
    private String contract_type;
    
    /**
     * 项目类别（传名称）
     */
    private String project_type;
    
    /**
     * 是否连锁（传编码）
     */
    private String link_yes;
    
    /**
     * 采购单位
     */
    private String pur_unit;
    
    /**
     * 战略单位名称
     */
    private String zhanl_name;
    
    /**
     * 行业（传名称）
     */
    private String industry;
    
    /**
     * 行业地位（传名称）
     */
    private String industry_dw;
    
    /**
     * 项目
     */
    private String project;
    
    /**
     * 项目名称
     */
    private String project_name;
    
    /**
     * 项目地址
     */
    private String project_addr;
    
    /**
     * 合同金额
     */
    private String contract_amount;
    
    /**
     * 合同起始日期
     */
    private String begin_date;
    
    /**
     * 合同截止日期
     */
    private String end_date;
    
    /**
     * 我司单位（传名称）
     */
    private String o_unit;
    
    /**
     * 合同份数
     */
    private String contract_num;
    
    /**
     * 备注
     */
    private String note;
    
    /**
     * 创建人
     */
    private String create_code;
    
    /**
     * 省编码
     */
    private String Code_prov;
    
    /**
     * 市编码
     */
    private String Code_city;
    
    /**
     * 区编码
     */
    private String Code_coun;
    
    /**
     * 镇编码
     */
    private String Code_town;
    
    /**
     * 经销商协议（传编码）
     */
    private String dealer_agreement;
    
    /**
     * 年份
     */
    private String year_contract;
    
    /**
     * 是否已签约（编码）
     */
    private String been_signed;
    
    /**
     * 考核比例
     */
    private String ass_rat;
} 