package com.seeyon.A8ContractPost.service;

import com.seeyon.A8ContractPost.model.*;

import java.util.List;
import java.util.Map;

/**
 * 合同服务接口
 */
public interface ContractService {
    
    /**
     * 获取Token
     *
     * @return Token字符串
     */
    String getToken();
    
    /**
     * 获取全国区域数据
     *
     * @param token    Token
     * @param codeProv 省份名称（可选）
     * @return 区域数据
     */
    List<AreaCode> getAreaCodes(String token, String codeProv);
    
    /**
     * 创建或更新合同
     *
     * @param token    Token
     * @param contract 合同数据
     * @return 合同创建响应
     */
    ContractCreateResponse createOrUpdateContract(String token, Contract contract);
    
    /**
     * 批量创建或更新合同
     *
     * @param token     Token
     * @param contracts 合同数据列表
     * @return 创建结果
     */
    Map<String, ContractCreateResponse> batchCreateOrUpdateContracts(String token, List<Contract> contracts);
    
    /**
     * 创建或更新合同任务
     *
     * @param token        Token
     * @param contractTask 合同任务数据
     * @return 操作结果
     */
    ApiResponse createOrUpdateContractTask(String token, ContractTask contractTask);
    
    /**
     * 批量创建或更新合同任务
     *
     * @param token         Token
     * @param contractTasks 合同任务数据列表
     * @return 操作结果
     */
    List<ApiResponse> batchCreateOrUpdateContractTasks(String token, List<ContractTask> contractTasks);
    
    /**
     * 创建或更新合同保证金
     *
     * @param token             Token
     * @param contractGuarantee 合同保证金数据
     * @return 操作结果
     */
    ApiResponse createOrUpdateContractGuarantee(String token, ContractGuarantee contractGuarantee);
    
    /**
     * 批量创建或更新合同保证金
     *
     * @param token              Token
     * @param contractGuarantees 合同保证金数据列表
     * @return 操作结果
     */
    List<ApiResponse> batchCreateOrUpdateContractGuarantees(String token, List<ContractGuarantee> contractGuarantees);
    
    /**
     * 更新合同区域
     *
     * @param token        Token
     * @param contractArea 合同区域数据
     * @return 操作结果
     */
    ApiResponse updateContractArea(String token, List<ContractArea> contractArea);
    
    /**
     * 删除合同
     *
     * @param token    Token
     * @param orderNo 合同号
     * @return 操作结果
     */
    ApiResponse deleteContract(String token, String orderNo);
} 