package com.seeyon.A8ContractPost.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 合同数据请求类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractDataRequest<T> extends ApiRequest {
    
    /**
     * 数据列表
     */
    private List<T> PreviousDataList;
} 