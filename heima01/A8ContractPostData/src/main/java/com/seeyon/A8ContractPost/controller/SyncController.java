package com.seeyon.A8ContractPost.controller;

import com.seeyon.A8ContractPost.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;
    
    @Value("${contract.sync.batch-size:50}")
    private int defaultBatchSize;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * 手动触发同步
     * 
     * @param batchSize 批量大小，可选参数
     * @return 同步结果
     */
    @PostMapping
    public Map<String, Object> triggerSync(@RequestParam(required = false) Integer batchSize) {
        log.info("手动触发同步任务，批量大小: {}", batchSize != null ? batchSize : defaultBatchSize);
        
        Map<String, Object> result = new HashMap<>();
        try {
            String syncResult = syncService.sync(batchSize != null ? batchSize : defaultBatchSize);
            result.put("success", true);
            result.put("message", syncResult);
        } catch (Exception e) {
            log.error("手动同步任务执行异常", e);
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 同步指定合同
     * 
     * @param contractId 合同ID
     * @return 同步结果
     */
    @PostMapping("/{contractId}")
    public Map<String, Object> syncContract(@PathVariable String contractId) {
        log.info("手动触发同步指定合同: {}", contractId);
        
        Map<String, Object> result = new HashMap<>();
        try {
            String syncResult = syncService.syncContract(contractId);
            result.put("success", true);
            result.put("message", syncResult);
        } catch (Exception e) {
            log.error("同步指定合同失败: {}", contractId, e);
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
        }
        
        return result;
    }
} 