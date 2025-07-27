package com.seeyon.A8ContractPost.service;

/**
 * 同步服务接口
 */
public interface SyncService {
    
    /**
     * 执行同步
     *
     * @param batchSize 批量处理大小
     * @return 同步结果
     */
    String sync(int batchSize);
    
    /**
     * 同步指定合同
     *
     * @param contractId 合同ID
     * @return 同步结果
     */
    String syncContract(String contractId);
} 