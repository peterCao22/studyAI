package com.seeyon.A8ContractPost.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 区域编码转换工具类
 * 用于处理OA系统和目标系统之间的区域编码转换
 */
@Slf4j
@Component
public class AreaCodeConverter {

    // 存储特殊的区域编码映射关系
    private final Map<String, String> specialMappings = new HashMap<>();

    /**
     * 初始化特殊映射关系
     */
    public AreaCodeConverter() {
        // 可以在这里添加一些特殊的映射关系
        // 例如：specialMappings.put("420117406", "420117000");
    }

    /**
     * 转换省级编码
     * OA系统：2位
     * 目标系统：6位（在2位后加4个0）
     *
     * @param oaProvCode OA系统省级编码
     * @return 目标系统省级编码
     */
    public String convertProvCode(String oaProvCode) {
        if (oaProvCode == null || oaProvCode.isEmpty()) {
            return "000000";
        }
        
        // 检查是否已经是6位
        if (oaProvCode.length() == 6) {
            return oaProvCode;
        }
        
        // 如果是2位，添加4个0
        if (oaProvCode.length() == 2) {
            return oaProvCode + "0000";
        }
        
        log.warn("无效的省级编码格式: {}", oaProvCode);
        return oaProvCode;
    }

    /**
     * 转换市级编码
     * OA系统：4位
     * 目标系统：6位（在4位后加2个0）
     *
     * @param oaCityCode OA系统市级编码
     * @return 目标系统市级编码
     */
    public String convertCityCode(String oaCityCode) {
        if (oaCityCode == null || oaCityCode.isEmpty()) {
            return "000000";
        }
        
        // 检查是否已经是6位
        if (oaCityCode.length() == 6) {
            return oaCityCode;
        }
        
        // 如果是4位，添加2个0
        if (oaCityCode.length() == 4) {
            return oaCityCode + "00";
        }
        
        log.warn("无效的市级编码格式: {}", oaCityCode);
        return oaCityCode;
    }

    /**
     * 转换县级编码
     * OA系统和目标系统编码一致，都是6位
     *
     * @param oaCountyCode OA系统县级编码
     * @return 目标系统县级编码
     */
    public String convertCountyCode(String oaCountyCode) {
        if (oaCountyCode == null || oaCountyCode.isEmpty()) {
            return "000000";
        }
        return oaCountyCode;
    }

    /**
     * 转换镇区街道编码
     * OA系统和目标系统编码一致，但可能存在OA系统有而目标系统没有的情况
     *
     * @param oaTownCode OA系统镇区街道编码
     * @return 目标系统镇区街道编码
     */
    public String convertTownCode(String oaTownCode) {
        if (oaTownCode == null || oaTownCode.isEmpty()) {
            return "000000000";
        }
        
        // 检查是否有特殊映射
        if (specialMappings.containsKey(oaTownCode)) {
            return specialMappings.get(oaTownCode);
        }
        
        return oaTownCode;
    }

    /**
     * 添加特殊映射关系
     *
     * @param oaCode     OA系统编码
     * @param targetCode 目标系统编码
     */
    public void addSpecialMapping(String oaCode, String targetCode) {
        specialMappings.put(oaCode, targetCode);
    }

    /**
     * 获取所有特殊映射关系
     *
     * @return 特殊映射关系Map
     */
    public Map<String, String> getSpecialMappings() {
        return new HashMap<>(specialMappings);
    }
} 