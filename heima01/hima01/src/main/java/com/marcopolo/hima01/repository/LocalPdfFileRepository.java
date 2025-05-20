package com.marcopolo.hima01.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import com.marcopolo.hima01.service.GeminiOcrService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalPdfFileRepository implements FileRepository {

    private final VectorStore vectorStore;
    private final GeminiOcrService geminiOcrService;

    // 会话id 与 文件名的对应关系，方便查询会话历史时重新加载文件
    private final Properties chatFiles = new Properties();

    @Override
    public boolean save(String chatId, Resource resource) {
        // 1.保存到本地磁盘
        String filename = resource.getFilename();
        File target = new File(Objects.requireNonNull(filename));
        if (!target.exists()) {
            try {
                Files.copy(resource.getInputStream(), target.toPath());
            } catch (IOException e) {
                log.error("Failed to save PDF resource.", e);
                return false;
            }
        }
        
        // 2.将PDF文件转换为向量并存入向量库
        try {
            // 创建PDF阅读器，先转换为FileSystemResource
            FileSystemResource fileResource = new FileSystemResource(target);
            PagePdfDocumentReader reader = new PagePdfDocumentReader(
                    fileResource,
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                            .withPagesPerDocument(1)  // 每页作为一个文档
                            .build()
            );

            // 读取PDF文档，拆分为Document列表
            List<Document> documents = reader.read();
            log.info("成功读取PDF文件: {}, 共{}页", filename, documents.size());
            
            // 检查是否为扫描PDF（无文本内容）
            boolean isScannedPdf = documents.isEmpty() || 
                    documents.stream().allMatch(doc -> doc.getText() == null || doc.getText().trim().isEmpty());
            
            // 扫描PDF使用Gemini进行OCR识别
            List<Document> enhancedDocuments;
            if (isScannedPdf) {
                log.info("检测到扫描格式PDF，尝试使用Gemini进行OCR识别: {}", filename);
                enhancedDocuments = geminiOcrService.extractTextWithGemini(target, filename, chatId);
                
                if (enhancedDocuments.isEmpty()) {
                    log.warn("Gemini OCR处理后没有提取到有效文本，跳过向量化");
                    chatFiles.put(chatId, filename);
                    return true;
                }
            } else {
                // 添加元数据（为了支持按文件名过滤）
                enhancedDocuments = documents.stream()
                    .map(doc -> {
                        // 检查文档文本是否为空
                        String text = doc.getText();
                        if (text == null || text.trim().isEmpty()) {
                            log.warn("文档页面内容为空，跳过该页面");
                            return null;
                        }
                        
                        // 确保文档有ID
                        String docId = doc.getId();
                        if (docId == null || docId.trim().isEmpty()) {
                            docId = UUID.randomUUID().toString();
                        }
                        
                        // 构建元数据
                        Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                        metadata.put("file_name", filename);  // 与PdfController中的过滤条件对应
                        metadata.put("chat_id", chatId);
                        metadata.put("page_number", metadata.getOrDefault("page_number", "unknown"));
                        
                        // 创建新的Document对象，保留原始内容，添加新的元数据
                        return new Document(docId, text, metadata);
                    })
                    .filter(Objects::nonNull) // 过滤掉空文档
                    .collect(Collectors.toList());
                
                if (enhancedDocuments.isEmpty()) {
                    log.warn("处理后没有有效文档内容，不进行向量化");
                    chatFiles.put(chatId, filename);
                    return true;
                }
            }
            
            // 调试：打印每页文档的内容摘要
            for (int i = 0; i < enhancedDocuments.size(); i++) {
                Document doc = enhancedDocuments.get(i);
                String pageText = doc.getText();
                String pageNum = doc.getMetadata().getOrDefault("page_number", "未知").toString();
                log.info("文档页面 #{} (页码:{}) 内容摘要(前300字符): {}", 
                        i+1, pageNum, pageText.substring(0, Math.min(pageText.length(), 300)));
            }
            
            // 添加到向量存储
            log.info("开始向量化文档，共{}页有效内容", enhancedDocuments.size());
            vectorStore.add(enhancedDocuments);
            log.info("PDF内容成功添加到向量库: {}, 元数据包含文件名: {}", filename, filename);
        } catch (Exception e) {
            log.error("Failed to vectorize PDF: {}", filename, e);
            // 打印更详细的错误信息
            log.error("详细错误信息:", e);
            // 文件保存成功但向量化失败，仍然返回true以保留文件
        }
        
        // 3.保存映射关系
        chatFiles.put(chatId, filename);
        return true;
    }

    @Override
    public Resource getFile(String chatId) {
        String filename = chatFiles.getProperty(chatId);
        if (filename == null) {
            throw new RuntimeException("找不到与chatId关联的文件: " + chatId);
        }
        return new FileSystemResource(filename);
    }

    @PostConstruct
    private void init() {
        // 1. 加载chatId和文件名的映射关系
        FileSystemResource propResource = new FileSystemResource("chat-pdf.properties");
        if (propResource.exists()) {
            try {
                chatFiles.load(new BufferedReader(new InputStreamReader(propResource.getInputStream(), StandardCharsets.UTF_8)));
                log.info("加载PDF映射关系成功，共{}个文件", chatFiles.size());
            } catch (IOException e) {
                log.error("Failed to load chat-pdf.properties", e);
                throw new RuntimeException(e);
            }
        }
        
        // 2. 初始化向量数据(如果有需要的话)
        // 注意：如果使用Milvus，向量数据已经持久化在Milvus数据库中，不需要额外加载
        // 此处可以遍历chatFiles中的所有文件，检查它们是否已经在向量库中
        // 如果需要重新加载，可以在此处实现
    }

    @PreDestroy
    private void persistent() {
        try {
            // 只保存chatId和文件名的映射关系
            chatFiles.store(new FileWriter("chat-pdf.properties"), LocalDateTime.now().toString());
            log.info("成功保存PDF映射关系，共{}个文件", chatFiles.size());
            
            // 注意：Milvus向量库是外部持久化存储，不需要额外保存
            // SimpleVectorStore需要手动保存，但MilvusVectorStore不需要
        } catch (IOException e) {
            log.error("Failed to store chat-pdf.properties", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据查询文本检索相似内容
     * @param query 查询文本
     * @param topK 返回结果数量
     * @return 相似文档列表
     */
    public List<Document> searchSimilarContent(String query, int topK) {
        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build()
        );
    }
}