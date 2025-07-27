package com.seeyon.A8ContractPost.config;

import com.seeyon.A8ContractPost.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 */
@Slf4j
@Configuration
public class ScheduleConfig {

    private final SyncService syncService;
    
    @Value("${contract.sync.batch-size:50}")
    private int batchSize;

    public ScheduleConfig(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * 定时同步合同数据
     * 配置从application.yml中的cron表达式获取
     */
    @Scheduled(cron = "${contract.sync.cron}")
    public void scheduledSync() {
        log.info("开始执行定时同步任务");
        try {
            String result = syncService.sync(batchSize);
            log.info("定时同步任务执行结果: {}", result);
        } catch (Exception e) {
            log.error("定时同步任务执行异常", e);
        }
    }
} 