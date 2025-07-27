package com.seeyon.A8ContractPost.dao;

import com.seeyon.A8ContractPost.model.Contract;
import com.seeyon.A8ContractPost.model.ContractArea;
import com.seeyon.A8ContractPost.model.ContractGuarantee;
import com.seeyon.A8ContractPost.model.ContractTask;

import java.util.List;

/**
 * 合同DAO接口
 */
public interface ContractDao {
    
    /**
     * 获取待同步的合同列表
     *
     * @param limit 限制数量
     * @return 合同列表
     */
    List<Contract> getContractsToSync(int limit);
    
    /**
     * 获取合同的任务列表
     *
     * @param orderNo 合同号
     * @return 任务列表
     */
    List<ContractTask> getContractTasks(String orderNo);
    
    /**
     * 获取合同的保证金列表
     *
     * @param orderNo 合同号
     * @return 保证金列表
     */
    List<ContractGuarantee> getContractGuarantees(String orderNo);
    
    /**
     * 获取合同的区域列表
     *
     * @param orderNo 合同号
     * @return 区域列表
     */
    List<ContractArea> getContractAreas(String orderNo);
    
    /**
     * 更新合同同步状态
     *
     * @param orderNo 合同号
     * @param status  同步状态
     * @param message 同步消息
     * @return 更新行数
     */
    int updateContractSyncStatus(String orderNo, int status, String message);
    
    /**
     * 记录同步日志
     *
     * @param orderNo 合同号
     * @param type    日志类型
     * @param content 日志内容
     * @return 插入行数
     */
    int logSync(String orderNo, String type, String content);
} 