package com.seeyon.A8ContractPost.dao;

import com.seeyon.A8ContractPost.model.Contract;
import com.seeyon.A8ContractPost.model.ContractArea;
import com.seeyon.A8ContractPost.model.ContractGuarantee;
import com.seeyon.A8ContractPost.model.ContractTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 合同DAO实现类
 */
@Slf4j
@Repository
public class ContractDaoImpl implements ContractDao {

    private final JdbcTemplate oaJdbcTemplate;
    private final JdbcTemplate localJdbcTemplate;

    public ContractDaoImpl(@Qualifier("oaJdbcTemplate") JdbcTemplate oaJdbcTemplate,
                           @Qualifier("localJdbcTemplate") JdbcTemplate localJdbcTemplate) {
        this.oaJdbcTemplate = oaJdbcTemplate;
        this.localJdbcTemplate = localJdbcTemplate;
        
        // 初始化本地数据库表
        initLocalTables();
    }

    /**
     * 初始化本地数据库表
     */
    private void initLocalTables() {
        try {
            // 创建同步状态表
            localJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contract_sync_status (" +
                    "order_no VARCHAR(50) PRIMARY KEY, " +
                    "status INT DEFAULT 0, " +
                    "message VARCHAR(500), " +
                    "sync_time TIMESTAMP, " +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            
            // 创建同步日志表
            localJdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contract_sync_log (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_no VARCHAR(50), " +
                    "type VARCHAR(50), " +
                    "content VARCHAR(4000), " +
                    "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            
            log.info("本地数据库表初始化成功");
        } catch (Exception e) {
            log.error("初始化本地数据库表失败", e);
            throw new RuntimeException("初始化本地数据库表失败", e);
        }
    }

    @Override
    public List<Contract> getContractsToSync(int limit) {
        // 这里需要根据实际OA系统的数据库结构编写SQL
        // 以下是示例SQL，需要根据实际情况修改
        String sql = "SELECT " +
                "c.contract_name, " +
                "TO_CHAR(c.signcontract_date, 'YYYY-MM-DD') as signcontract_date, " +
                "c.first_check, " +
                "c.recheck, " +
                "c.signcontract_man, " +
                "c.property, " +
                "c.cust_no, " +
                "c.contract_type, " +
                "c.project_type, " +
                "c.link_yes, " +
                "c.pur_unit, " +
                "c.zhanl_name, " +
                "c.industry, " +
                "c.industry_dw, " +
                "c.project, " +
                "c.project_name, " +
                "c.project_addr, " +
                "c.contract_amount, " +
                "TO_CHAR(c.begin_date, 'YYYY-MM-DD') as begin_date, " +
                "TO_CHAR(c.end_date, 'YYYY-MM-DD') as end_date, " +
                "c.o_unit, " +
                "c.contract_num, " +
                "c.note, " +
                "c.create_code, " +
                "c.Code_prov, " +
                "c.Code_city, " +
                "c.Code_coun, " +
                "c.Code_town, " +
                "c.dealer_agreement, " +
                "c.year_contract, " +
                "c.been_signed, " +
                "c.ass_rat " +
                "FROM contract c " +
                "LEFT JOIN contract_sync_status s ON c.contract_no = s.order_no " +
                "WHERE s.order_no IS NULL OR s.status = 0 " +
                "ORDER BY c.create_time DESC " +
                "LIMIT ?";
        
        try {
            return oaJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Contract.class), limit);
        } catch (Exception e) {
            log.error("获取待同步合同列表失败", e);
            throw new RuntimeException("获取待同步合同列表失败", e);
        }
    }

    @Override
    public List<ContractTask> getContractTasks(String orderNo) {
        // 这里需要根据实际OA系统的数据库结构编写SQL
        // 以下是示例SQL，需要根据实际情况修改
        String sql = "SELECT " +
                "'ADD' as flag, " +
                "'0' as id, " +
                "? as order_no, " +
                "t.start_month, " +
                "t.end_month, " +
                "t.sale_amount, " +
                "t.return_amount " +
                "FROM contract_task t " +
                "WHERE t.contract_no = ?";
        
        try {
            return oaJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ContractTask.class), orderNo, orderNo);
        } catch (Exception e) {
            log.error("获取合同任务列表失败: {}", orderNo, e);
            throw new RuntimeException("获取合同任务列表失败", e);
        }
    }

    @Override
    public List<ContractGuarantee> getContractGuarantees(String orderNo) {
        // 这里需要根据实际OA系统的数据库结构编写SQL
        // 以下是示例SQL，需要根据实际情况修改
        String sql = "SELECT " +
                "'ADD' as flag, " +
                "'0' as id, " +
                "? as order_no, " +
                "g.guarantee_no, " +
                "g.guarantee_amount, " +
                "g.guarantee_note " +
                "FROM contract_guarantee g " +
                "WHERE g.contract_no = ?";
        
        try {
            return oaJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ContractGuarantee.class), orderNo, orderNo);
        } catch (Exception e) {
            log.error("获取合同保证金列表失败: {}", orderNo, e);
            throw new RuntimeException("获取合同保证金列表失败", e);
        }
    }

    @Override
    public List<ContractArea> getContractAreas(String orderNo) {
        // 这里需要根据实际OA系统的数据库结构编写SQL
        // 以下是示例SQL，需要根据实际情况修改
        String sql = "SELECT " +
                "? as order_no, " +
                "a.code_prov, " +
                "a.code_city, " +
                "a.code_coun, " +
                "a.code_town " +
                "FROM contract_area a " +
                "WHERE a.contract_no = ?";
        
        try {
            return oaJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ContractArea.class), orderNo, orderNo);
        } catch (Exception e) {
            log.error("获取合同区域列表失败: {}", orderNo, e);
            throw new RuntimeException("获取合同区域列表失败", e);
        }
    }

    @Override
    public int updateContractSyncStatus(String orderNo, int status, String message) {
        String sql = "MERGE INTO contract_sync_status s " +
                "USING (SELECT ? as order_no) t " +
                "ON (s.order_no = t.order_no) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET s.status = ?, s.message = ?, s.sync_time = ?, s.update_time = CURRENT_TIMESTAMP " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (order_no, status, message, sync_time) " +
                "  VALUES (?, ?, ?, ?)";
        
        Timestamp syncTime = new Timestamp(System.currentTimeMillis());
        
        try {
            return localJdbcTemplate.update(sql, orderNo, status, message, syncTime, orderNo, status, message, syncTime);
        } catch (Exception e) {
            log.error("更新合同同步状态失败: {}", orderNo, e);
            throw new RuntimeException("更新合同同步状态失败", e);
        }
    }

    @Override
    public int logSync(String orderNo, String type, String content) {
        String sql = "INSERT INTO contract_sync_log (order_no, type, content) VALUES (?, ?, ?)";
        
        try {
            return localJdbcTemplate.update(sql, orderNo, type, content);
        } catch (Exception e) {
            log.error("记录同步日志失败: {}", orderNo, e);
            throw new RuntimeException("记录同步日志失败", e);
        }
    }
} 