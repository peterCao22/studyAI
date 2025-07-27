package com.seeyon.A8ContractPost.service;

import com.seeyon.A8ContractPost.model.*;
import com.seeyon.A8ContractPost.util.AreaCodeConverter;
import com.seeyon.A8ContractPost.util.HttpClientUtil;
import com.seeyon.A8ContractPost.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 合同服务实现类
 */
@Slf4j
@Service
public class ContractServiceImpl implements ContractService {

    private final HttpClientUtil httpClientUtil;
    private final AreaCodeConverter areaCodeConverter;

    @Value("${contract.api.base-url}")
    private String baseUrl;

    @Value("${contract.api.username}")
    private String username;

    @Value("${contract.api.password}")
    private String password;

    @Value("${contract.api.lang-code}")
    private String langCode;

    @Value("${contract.api.token-ttl}")
    private long tokenTtl;

    // 缓存Token及其过期时间
    private String cachedToken;
    private long tokenExpireTime;

    // 缓存区域数据
    private final Map<String, List<AreaCode>> areaCodeCache = new ConcurrentHashMap<>();

    public ContractServiceImpl(HttpClientUtil httpClientUtil, AreaCodeConverter areaCodeConverter) {
        this.httpClientUtil = httpClientUtil;
        this.areaCodeConverter = areaCodeConverter;
    }

    @Override
    public String getToken() {
        // 检查Token是否已缓存且未过期
        long currentTime = System.currentTimeMillis();
        if (cachedToken != null && currentTime < tokenExpireTime) {
            log.debug("使用缓存的Token");
            return cachedToken;
        }

        // 构建登录请求
        LoginRequest loginRequest = new LoginRequest(username, password, langCode);
        String requestJson = JsonUtil.toJson(loginRequest);
        
        // 发送登录请求
        String url = baseUrl + "/GetData/DataAnalysisLogin";
        String responseJson = httpClientUtil.doPost(url, requestJson);
        
        // 解析响应
        LoginResponse loginResponse = JsonUtil.fromJson(responseJson, LoginResponse.class);
        
        if (loginResponse.isSuccess()) {
            // 缓存Token
            cachedToken = loginResponse.getMess();
            tokenExpireTime = currentTime + tokenTtl;
            log.info("成功获取Token: {}", cachedToken);
            return cachedToken;
        } else {
            log.error("获取Token失败: {}", loginResponse.getMess());
            throw new RuntimeException("获取Token失败: " + loginResponse.getMess());
        }
    }

    // 全国区域数据（get)
    @Override
    public List<AreaCode> getAreaCodes(String token, String codeProv) {
        // 检查缓存
        String cacheKey = codeProv == null ? "ALL" : codeProv;
        if (areaCodeCache.containsKey(cacheKey)) {
            log.debug("使用缓存的区域数据: {}", cacheKey);
            return areaCodeCache.get(cacheKey);
        }

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("Token", token);
        if (codeProv != null && !codeProv.isEmpty()) {
            params.put("code_prov", codeProv);
        }

        // 发送请求
        String url = baseUrl + "/Contract/ContractTownshipArea";
        String responseJson = httpClientUtil.doGet(url, params);

        // 解析响应
        // 这里需要根据实际响应格式解析区域数据
        // 假设响应是一个区域数据列表
        List<AreaCode> areaCodes = parseAreaCodes(responseJson);
        
        // 缓存结果
        areaCodeCache.put(cacheKey, areaCodes);
        
        return areaCodes;
    }

    /**
     * 解析区域数据响应
     * 根据接口返回的JSON格式解析区域数据
     */
    private List<AreaCode> parseAreaCodes(String responseJson) {
        List<AreaCode> areaCodes = new ArrayList<>();
        
        try {
            // 解析JSON为Map
            Map<String, Object> responseMap = JsonUtil.fromJsonToMap(responseJson);
            
            // 检查状态码
            int state = (int) responseMap.get("state");
            if (state != 1) {
                // 如果状态不为1，表示请求失败
                String message = responseMap.containsKey("mess") ? (String) responseMap.get("mess") : "未知错误";
                log.error("获取区域编码失败: {}", message);
                return areaCodes;
            }
            
            // 获取InfoList数组
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> infoList = (List<Map<String, Object>>) responseMap.get("InfoList");
            if (infoList == null || infoList.isEmpty()) {
                log.error("区域编码数据格式错误: InfoList不是数组或不存在");
                return areaCodes;
            }
            
            // 解析每个区域信息
            for (Map<String, Object> item : infoList) {
                // 省级
                AreaCode province = new AreaCode();
                province.setCode((String) item.get("code_prov"));
                province.setName((String) item.get("name_prov"));
                province.setParentCode("0"); // 省级没有父级，设为0
                province.setLevel(1);
                addUniqueAreaCode(areaCodes, province);
                
                // 市级
                AreaCode city = new AreaCode();
                city.setCode((String) item.get("code_city"));
                city.setName((String) item.get("name_city"));
                city.setParentCode(province.getCode());
                city.setLevel(2);
                addUniqueAreaCode(areaCodes, city);
                
                // 区/县级
                AreaCode county = new AreaCode();
                county.setCode((String) item.get("code_coun"));
                county.setName((String) item.get("name_coun"));
                county.setParentCode(city.getCode());
                county.setLevel(3);
                addUniqueAreaCode(areaCodes, county);
                
                // 镇/街道级
                AreaCode town = new AreaCode();
                town.setCode((String) item.get("code_town"));
                town.setName((String) item.get("name_town"));
                town.setParentCode(county.getCode());
                town.setLevel(4);
                addUniqueAreaCode(areaCodes, town);
            }
            
            log.info("成功解析区域编码数据，共 {} 条记录", areaCodes.size());
        } catch (Exception e) {
            log.error("解析区域编码数据异常", e);
        }
        
        return areaCodes;
    }
    
    /**
     * 添加唯一的区域编码到列表中（避免重复）
     */
    private void addUniqueAreaCode(List<AreaCode> areaCodes, AreaCode areaCode) {
        // 检查是否已存在相同编码的记录
        boolean exists = areaCodes.stream()
                .anyMatch(item -> item.getCode().equals(areaCode.getCode()));
        
        if (!exists) {
            areaCodes.add(areaCode);
        }
    }

    // 维护经销商合同(post)
    @Override
    public ContractCreateResponse createOrUpdateContract(String token, Contract contract) {
        // 转换区域编码
        contract.setCode_prov(areaCodeConverter.convertProvCode(contract.getCode_prov()));
        contract.setCode_city(areaCodeConverter.convertCityCode(contract.getCode_city()));
        contract.setCode_coun(areaCodeConverter.convertCountyCode(contract.getCode_coun()));
        contract.setCode_town(areaCodeConverter.convertTownCode(contract.getCode_town()));

        // 构建请求
        ContractDataRequest<Contract> request = new ContractDataRequest<>();
        request.setToken(token);
        request.setLangz_Code(langCode);
        request.setPreviousDataList(Collections.singletonList(contract));

        // 发送请求
        String url = baseUrl + "/Contract/ContractAddorUpdDoc";
        String requestJson = JsonUtil.toJson(request);
        String responseJson = httpClientUtil.doPost(url, requestJson);

        // 解析响应
        ContractCreateResponse response = JsonUtil.fromJson(responseJson, ContractCreateResponse.class);
        
        if (response.isSuccess()) {
            log.info("创建/更新合同成功: {}", response.getOrder_no());
        } else {
            log.error("创建/更新合同失败: {}", response.getMess());
        }
        
        return response;
    }

    @Override
    public Map<String, ContractCreateResponse> batchCreateOrUpdateContracts(String token, List<Contract> contracts) {
        Map<String, ContractCreateResponse> results = new HashMap<>();
        
        for (Contract contract : contracts) {
            try {
                ContractCreateResponse response = createOrUpdateContract(token, contract);
                results.put(contract.getContract_name(), response);
            } catch (Exception e) {
                log.error("处理合同时出错: {}", contract.getContract_name(), e);
                ContractCreateResponse errorResponse = new ContractCreateResponse();
                errorResponse.setState(0);
                errorResponse.setMess("处理出错: " + e.getMessage());
                results.put(contract.getContract_name(), errorResponse);
            }
        }
        
        return results;
    }

    // 维护合同任务,已创建的合同号，添加销售和回笼任务。
    @Override
    public ApiResponse createOrUpdateContractTask(String token, ContractTask contractTask) {
        // 构建请求
        ContractDataRequest<ContractTask> request = new ContractDataRequest<>();
        request.setToken(token);
        request.setLangz_Code(langCode);
        request.setPreviousDataList(Collections.singletonList(contractTask));

        // 发送请求
        String url = baseUrl + "/Contract/ContractAddorUpdTask";
        String requestJson = JsonUtil.toJson(request);
        String responseJson = httpClientUtil.doPost(url, requestJson);

        // 解析响应
        ApiResponse response = JsonUtil.fromJson(responseJson, ApiResponse.class);
        
        if (response.isSuccess()) {
            log.info("创建/更新合同任务成功: {}", contractTask.getOrder_no());
        } else {
            log.error("创建/更新合同任务失败: {}", response.getMess());
        }
        
        return response;
    }

    @Override
    public List<ApiResponse> batchCreateOrUpdateContractTasks(String token, List<ContractTask> contractTasks) {
        return contractTasks.stream()
                .map(task -> {
                    try {
                        return createOrUpdateContractTask(token, task);
                    } catch (Exception e) {
                        log.error("处理合同任务时出错: {}", task.getOrder_no(), e);
                        ApiResponse errorResponse = new ApiResponse();
                        errorResponse.setState(0);
                        errorResponse.setMess("处理出错: " + e.getMessage());
                        return errorResponse;
                    }
                })
                .collect(Collectors.toList());
    }

    // 维护合同保证金（post)
    @Override
    public ApiResponse createOrUpdateContractGuarantee(String token, ContractGuarantee contractGuarantee) {
        // 构建请求
        ContractDataRequest<ContractGuarantee> request = new ContractDataRequest<>();
        request.setToken(token);
        request.setLangz_Code(langCode);
        request.setPreviousDataList(Collections.singletonList(contractGuarantee));

        // 发送请求
        String url = baseUrl + "/Contract/ContractAddorUpdGuaranteeAmount";
        String requestJson = JsonUtil.toJson(request);
        String responseJson = httpClientUtil.doPost(url, requestJson);

        // 解析响应
        ApiResponse response = JsonUtil.fromJson(responseJson, ApiResponse.class);
        
        if (response.isSuccess()) {
            log.info("创建/更新合同保证金成功: {}", contractGuarantee.getOrder_no());
        } else {
            log.error("创建/更新合同保证金失败: {}", response.getMess());
        }
        
        return response;
    }

    @Override
    public List<ApiResponse> batchCreateOrUpdateContractGuarantees(String token, List<ContractGuarantee> contractGuarantees) {
        return contractGuarantees.stream()
                .map(guarantee -> {
                    try {
                        return createOrUpdateContractGuarantee(token, guarantee);
                    } catch (Exception e) {
                        log.error("处理合同保证金时出错: {}", guarantee.getOrder_no(), e);
                        ApiResponse errorResponse = new ApiResponse();
                        errorResponse.setState(0);
                        errorResponse.setMess("处理出错: " + e.getMessage());
                        return errorResponse;
                    }
                })
                .collect(Collectors.toList());
    }

    // 维护合同区域（post)
    @Override
    public ApiResponse updateContractArea(String token, List<ContractArea> contractAreas) {
        // 转换区域编码
        for (ContractArea area : contractAreas) {
            area.setCode_prov(areaCodeConverter.convertProvCode(area.getCode_prov()));
            area.setCode_city(areaCodeConverter.convertCityCode(area.getCode_city()));
            area.setCode_coun(areaCodeConverter.convertCountyCode(area.getCode_coun()));
            area.setCode_town(areaCodeConverter.convertTownCode(area.getCode_town()));
        }

        // 构建请求
        ContractDataRequest<ContractArea> request = new ContractDataRequest<>();
        request.setToken(token);
        request.setLangz_Code(langCode);
        request.setPreviousDataList(contractAreas);

        // 发送请求
        String url = baseUrl + "/Contract/ContractNationalAreaAddOrUpd";
        String requestJson = JsonUtil.toJson(request);
        String responseJson = httpClientUtil.doPost(url, requestJson);

        // 解析响应
        ApiResponse response = JsonUtil.fromJson(responseJson, ApiResponse.class);
        
        if (response.isSuccess()) {
            log.info("更新合同区域成功: {}", contractAreas.get(0).getOrder_no());
        } else {
            log.error("更新合同区域失败: {}", response.getMess());
        }
        
        return response;
    }

    // 删除经销商合同(post)
    @Override
    public ApiResponse deleteContract(String token, String orderNo) {
        // 构建请求
        ContractDataRequest<Map<String, String>> request = new ContractDataRequest<>();
        request.setToken(token);
        request.setLangz_Code(langCode);
        
        Map<String, String> data = new HashMap<>();
        data.put("order_no", orderNo);
        request.setPreviousDataList(Collections.singletonList(data));

        // 发送请求
        String url = baseUrl + "/Contract/ContractDelDoc";
        String requestJson = JsonUtil.toJson(request);
        String responseJson = httpClientUtil.doPost(url, requestJson);

        // 解析响应
        ApiResponse response = JsonUtil.fromJson(responseJson, ApiResponse.class);
        
        if (response.isSuccess()) {
            log.info("删除合同成功: {}", orderNo);
        } else {
            log.error("删除合同失败: {}", response.getMess());
        }
        
        return response;
    }
} 