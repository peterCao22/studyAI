package com.seeyon.A8ContractPost.service;

import com.seeyon.A8ContractPost.dao.ContractDao;
import com.seeyon.A8ContractPost.model.*;
import com.seeyon.A8ContractPost.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 同步服务实现类
 */
@Slf4j
@Service
public class SyncServiceImpl implements SyncService {

    private final ContractService contractService;
    private final ContractDao contractDao;

    @Value("${contract.sync.batch-size:50}")
    private int defaultBatchSize;

    public SyncServiceImpl(ContractService contractService, ContractDao contractDao) {
        this.contractService = contractService;
        this.contractDao = contractDao;
    }

    @Override
    public String sync(int batchSize) {
        if (batchSize <= 0) {
            batchSize = defaultBatchSize;
        }

        log.info("开始同步合同数据，批量大小: {}", batchSize);
        
        // 获取Token
        String token;
        try {
            token = contractService.getToken();
        } catch (Exception e) {
            log.error("获取Token失败", e);
            return "同步失败: 获取Token失败 - " + e.getMessage();
        }

        // 获取待同步的合同列表
        List<Contract> contracts;
        try {
            contracts = contractDao.getContractsToSync(batchSize);
        } catch (Exception e) {
            log.error("获取待同步合同列表失败", e);
            return "同步失败: 获取待同步合同列表失败 - " + e.getMessage();
        }

        if (contracts.isEmpty()) {
            log.info("没有需要同步的合同数据");
            return "同步成功: 没有需要同步的合同数据";
        }

        log.info("找到 {} 个待同步合同", contracts.size());

        // 同步计数器
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 同步合同数据
        for (Contract contract : contracts) {
            try {
                syncSingleContract(token, contract);
                successCount.incrementAndGet();
            } catch (Exception e) {
                log.error("同步合同失败: {}", contract.getContract_name(), e);
                contractDao.updateContractSyncStatus(contract.getOrder_no(), 0, "同步失败: " + e.getMessage());
                contractDao.logSync(contract.getOrder_no(), "ERROR", e.getMessage());
                failCount.incrementAndGet();
            }
        }

        String result = String.format("同步完成: 成功 %d 个, 失败 %d 个", successCount.get(), failCount.get());
        log.info(result);
        return result;
    }

    @Override
    public String syncContract(String contractId) {
        log.info("开始同步指定合同: {}", contractId);
        
        // 获取Token
        String token;
        try {
            token = contractService.getToken();
        } catch (Exception e) {
            log.error("获取Token失败", e);
            return "同步失败: 获取Token失败 - " + e.getMessage();
        }

        // 这里需要根据实际情况获取合同数据
        // 暂时使用一个空的Contract对象
        Contract contract = new Contract();
        contract.setOrder_no(contractId);

        try {
            syncSingleContract(token, contract);
            return "同步成功: " + contractId;
        } catch (Exception e) {
            log.error("同步合同失败: {}", contractId, e);
            return "同步失败: " + e.getMessage();
        }
    }

    /**
     * 同步单个合同
     *
     * @param token    Token
     * @param contract 合同数据
     */
    private void syncSingleContract(String token, Contract contract) {
        String orderNo = contract.getOrder_no();
        
        // 记录开始同步
        log.info("开始同步合同 [{}]: {}", orderNo, contract.getContract_name());
        contractDao.logSync(orderNo, "START", "开始同步合同: " + contract.getContract_name());

        // 1. 创建或更新合同
        ContractCreateResponse createResponse = contractService.createOrUpdateContract(token, contract);
        if (!createResponse.isSuccess()) {
            log.error("创建合同失败 [{}]: {}", orderNo, createResponse.getMess());
            throw new RuntimeException("创建合同失败: " + createResponse.getMess());
        }
        
        // 获取生成的合同号
        String newOrderNo = createResponse.getOrder_no();
        log.info("创建合同成功 [{}] -> [{}]", orderNo, newOrderNo);
        contractDao.logSync(orderNo, "CREATE", "创建合同成功，合同号: " + newOrderNo);

        // 2. 同步合同任务，添加销售和回笼任务
        List<ContractTask> tasks = contractDao.getContractTasks(orderNo);
        log.info("获取到合同任务 [{}] -> [{}]: {} 个", orderNo, newOrderNo, tasks.size());
        if (!tasks.isEmpty()) {
            for (ContractTask task : tasks) {
                task.setOrder_no(newOrderNo);
                log.debug("同步合同任务 [{}]: 销售={}, 回笼={}", newOrderNo, task.getSale_amount(), task.getReturn_amount());
                ApiResponse taskResponse = contractService.createOrUpdateContractTask(token, task);
                if (!taskResponse.isSuccess()) {
                    log.error("创建合同任务失败 [{}]: {}", newOrderNo, taskResponse.getMess());
                    throw new RuntimeException("创建合同任务失败: " + taskResponse.getMess());
                }
            }
            log.info("创建合同任务成功 [{}]: {} 个", newOrderNo, tasks.size());
            contractDao.logSync(orderNo, "TASK", "创建合同任务成功，数量: " + tasks.size());
        }

        // 3. 同步合同保证金
        List<ContractGuarantee> guarantees = contractDao.getContractGuarantees(orderNo);
        log.info("获取到合同保证金 [{}] -> [{}]: {} 个", orderNo, newOrderNo, guarantees.size());
        if (!guarantees.isEmpty()) {
            for (ContractGuarantee guarantee : guarantees) {
                guarantee.setOrder_no(newOrderNo);
                log.debug("同步合同保证金 [{}]: 金额={}", newOrderNo, guarantee.getGuarantee_amount());
                ApiResponse guaranteeResponse = contractService.createOrUpdateContractGuarantee(token, guarantee);
                if (!guaranteeResponse.isSuccess()) {
                    log.error("创建合同保证金失败 [{}]: {}", newOrderNo, guaranteeResponse.getMess());
                    throw new RuntimeException("创建合同保证金失败: " + guaranteeResponse.getMess());
                }
            }
            log.info("创建合同保证金成功 [{}]: {} 个", newOrderNo, guarantees.size());
            contractDao.logSync(orderNo, "GUARANTEE", "创建合同保证金成功，数量: " + guarantees.size());
        }

        // 4. 同步合同区域（列表）
        List<ContractArea> areas = contractDao.getContractAreas(orderNo);
        log.info("获取到合同区域 [{}] -> [{}]: {} 个", orderNo, newOrderNo, areas.size());
        if (!areas.isEmpty()) {
            for (ContractArea area : areas) {
                area.setOrder_no(newOrderNo);
            }
            log.debug("同步合同区域 [{}]", newOrderNo);
            ApiResponse areaResponse = contractService.updateContractArea(token, areas);
            if (!areaResponse.isSuccess()) {
                log.error("更新合同区域失败 [{}]: {}", newOrderNo, areaResponse.getMess());
                throw new RuntimeException("更新合同区域失败: " + areaResponse.getMess());
            }
            log.info("更新合同区域成功 [{}]: {} 个", newOrderNo, areas.size());
            contractDao.logSync(orderNo, "AREA", "更新合同区域成功，数量: " + areas.size());
        }

        // 更新同步状态
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("orderNo", newOrderNo);
        resultMap.put("tasks", tasks.size());
        resultMap.put("guarantees", guarantees.size());
        resultMap.put("areas", areas.size());
        
        contractDao.updateContractSyncStatus(orderNo, 1, JsonUtil.toJson(resultMap));
        contractDao.logSync(orderNo, "COMPLETE", "同步完成");
        
        log.info("合同同步成功: [{}] -> [{}]", orderNo, newOrderNo);
    }
} 