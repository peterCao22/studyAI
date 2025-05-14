package com.marcopolo.hima01.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchEvaluator {
    
    public static class SearchEvalResult {
        private final boolean needsSearch;
        private final String searchResult;
        public SearchEvalResult(boolean needsSearch, String searchResult) {
            this.needsSearch = needsSearch;
            this.searchResult = searchResult;
        }
        public boolean isNeedsSearch() { return needsSearch; }
        public String getSearchResult() { return searchResult; }
    }

    @Autowired
    @Qualifier("searchChatClient")
    private ChatClient searchChatClient;
    
    // 需要搜索的关键词列表
    private static final List<String> TIME_KEYWORDS = List.of(
            // 时间相关关键词
            "最新", "今天", "现在", "新闻", "最近", "当前", "2024",
            "现状", "目前", "如今", "近期",
            
            // 数据相关关键词
            "价格", "股票", "汇率", "天气"
    );
    
    // 年份模式，用于检测回答中是否包含2023年之后的年份
    private static final Pattern YEAR_PATTERN = Pattern.compile("(202[3-9]|20[3-9][0-9])年");
    
    // 搜索相关短语
    private static final List<String> SEARCH_PHRASES = List.of(
            "根据最新信息", "最新数据显示", "最近的报道", "根据搜索结果", 
            "查询显示", "搜索发现", "根据网络信息", "最新研究表明"
    );

    /**
     * 判断是否需要搜索
     */
    public SearchEvalResult shouldSearch(String prompt) {
        log.debug("开始评估问题是否需要搜索: '{}'", prompt);
        
        // 先通过关键词快速判断
        boolean keywordMatch = fallbackKeywordCheck(prompt);
        log.debug("关键词检查结果: {}", keywordMatch);
        
        if(keywordMatch) {
            log.debug("通过关键词判断需要搜索，跳过模型评估");
            return new SearchEvalResult(true, null);
        }
        
        // 关键词未匹配到，使用模型判断
        try {
            String systemPrompt = """
                    你的任务是分析用户问题是否需要通过搜索获取最新信息。
                    
                    分析步骤：
                    1. 如果问题涉及2023年之后的事件、当前数据、最新消息、实时信息，需要搜索
                    2. 如果问题可以用通用知识回答，不需要搜索
                    
                    回答格式要求：
                    - 如果需要搜索，返回"search:true"
                    - 如果不需要搜索，返回"search:false"
                    
                    示例1：
                    问题：今天的天气怎么样？
                    回答：search:true
                    
                    示例2：
                    问题：水的化学式是什么？
                    回答：search:false
                    
                    严格遵循格式要求，不要添加任何解释或其他文字。
                    """;
            
            String response = searchChatClient.prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .call()
                    .content()
                    .trim();

            log.debug("搜索判断响应: {}", response);
            
            // 1. 检查是否符合规范格式
            if (response.contains("search:true")) {
                log.debug("模型返回规范格式，需要搜索");
                return new SearchEvalResult(true, null);
            } else if (response.contains("search:false")) {
                log.debug("模型返回规范格式，不需要搜索");
                return new SearchEvalResult(false, null);
            }
            
            // 2. 检查其他常见格式
            if (response.equalsIgnoreCase("true") || response.equalsIgnoreCase("需要搜索")) {
                log.debug("模型返回简化格式，需要搜索");
                return new SearchEvalResult(true, null);
            } else if (response.equalsIgnoreCase("false") || response.equalsIgnoreCase("不需要搜索")) {
                log.debug("模型返回简化格式，不需要搜索");
                return new SearchEvalResult(false, null);
            }
            
            // 3. 模型没有按照指令返回，进行内容智能分析
            log.debug("模型未按提示词返回，进行智能分析");
            
            // 3.1 检查是否包含近期年份
            boolean containsRecentYear = YEAR_PATTERN.matcher(response).find();
            
            // 3.2 检查是否包含链接
            boolean containsLinks = response.contains("http");
            
            // 3.3 检查是否包含搜索相关短语
            boolean containsSearchPhrases = SEARCH_PHRASES.stream().anyMatch(response::contains);
            
            // 3.4 判断模型是否可能已经执行了搜索（是否有引用网站、数据来源等）
            boolean hasPerformedSearch = response.contains("根据") && 
                   (response.contains("数据") || response.contains("报道") || 
                    response.contains("信息") || response.contains("公布"));
            
            // 综合判断
            boolean needsSearch = containsRecentYear || containsLinks || 
                                 containsSearchPhrases || hasPerformedSearch;
            
            log.debug("智能分析结果 - 包含近期年份: {}, 包含链接: {}, 包含搜索短语: {}, 执行了搜索: {}", 
                     containsRecentYear, containsLinks, containsSearchPhrases, hasPerformedSearch);
            log.debug("最终搜索判断结果: {}", needsSearch);
            
            // 如果模型内容很完整，直接返回内容
            if (needsSearch && (containsRecentYear || containsLinks)) {
                log.debug("模型已输出完整查找结果，直接复用");
                return new SearchEvalResult(true, response);
            }
            return new SearchEvalResult(needsSearch, null);
        } catch (Exception e) {
            log.error("模型评估搜索需求失败", e);
            return new SearchEvalResult(false, null);
        }
    }

    private boolean fallbackKeywordCheck(String prompt) {
        for (String keyword : TIME_KEYWORDS) {
            if (prompt.contains(keyword)) {
                log.debug("匹配到关键词: '{}' 在问题 '{}'中", keyword, prompt);
                return true;
            }
        }
        log.debug("未在问题中找到任何需要搜索的关键词");
        return false;
    }
} 