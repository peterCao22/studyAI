package com.marcopolo.hima01.controller;


import com.marcopolo.hima01.entity.vo.Result;
import com.marcopolo.hima01.repository.ChatHistoryRepository;
import com.marcopolo.hima01.repository.FileRepository;
import com.marcopolo.hima01.repository.LocalPdfFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor.FILTER_EXPRESSION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/pdf")
public class PdfController {

    private final FileRepository fileRepository;

    private final LocalPdfFileRepository localPdfFileRepository;

    private final VectorStore vectorStore;

    private final ChatClient pdfChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId) {
        // 1.找到会话文件
        Resource file = fileRepository.getFile(chatId);
        if (!file.exists()) {
            // 文件不存在，不回答
            throw new RuntimeException("会话文件不存在！");
        }
        // 2.保存会话id
        chatHistoryRepository.save("pdf", chatId);
        // 3.请求模型
        return pdfChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(a -> a.param(FILTER_EXPRESSION, "file_name == '" + file.getFilename() + "'"))
                .stream()
                .content();
    }

    /**
     * 文件上传
     */
    @RequestMapping("/upload/{chatId}")
    public Result uploadPdf(@PathVariable String chatId, @RequestParam("file") MultipartFile file) {
        try {
            // 1. 校验文件是否为PDF格式
            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return Result.fail("只能上传PDF文件！");
            }
            // 2.保存文件
            boolean success = fileRepository.save(chatId, file.getResource());
            if (!success) {
                return Result.fail("保存文件失败！");
            }
            return Result.ok();
        } catch (Exception e) {
            log.error("Failed to upload PDF.", e);
            return Result.fail("上传文件失败！");
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> download(@PathVariable("chatId") String chatId) throws IOException {
        // 1.读取文件
        Resource resource = fileRepository.getFile(chatId);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        // 2.文件名编码，写入响应头
        String filename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8);
        // 3.返回文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
    
    /**
     * 检测PDF是否为扫描PDF，并提供PDF信息
     */
    @RequestMapping("/analyze/{chatId}")
    public Result analyzePdf(@PathVariable String chatId) {
        try {
            // 获取文件
            Resource file = fileRepository.getFile(chatId);
            if (!file.exists()) {
                return Result.fail("文件不存在");
            }
            
            // 创建PDF阅读器
            PagePdfDocumentReader reader = new PagePdfDocumentReader(
                    file,
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                            .withPagesPerDocument(1)  // 每页作为一个文档
                            .build()
            );
            
            // 尝试读取文档
            List<Document> documents = reader.read();
            
            // 检查是否为扫描PDF（无文本内容）
            boolean isScannedPdf = documents.isEmpty() || 
                    documents.stream().allMatch(doc -> doc.getText() == null || doc.getText().trim().isEmpty());
            
            // 统计页面信息
            int totalPages = documents.size();
            int emptyPages = (int) documents.stream()
                    .filter(doc -> doc.getText() == null || doc.getText().trim().isEmpty())
                    .count();
            
            // 查询该文件在向量库中的文档数
            List<Document> vectorDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .filterExpression("file_name == '" + file.getFilename() + "'")
                    .topK(1)
                    .build()
            );
            
            // 返回分析结果
            Map<String, Object> data = new HashMap<>();
            data.put("filename", file.getFilename());
            data.put("chatId", chatId);
            data.put("isScannedPdf", isScannedPdf);
            data.put("totalPages", totalPages);
            data.put("emptyPages", emptyPages);
            data.put("textPages", totalPages - emptyPages);
            data.put("vectorDocuments", vectorDocs.size());
            data.put("geminiOcrProcessed", !vectorDocs.isEmpty() && vectorDocs.get(0).getMetadata().containsKey("is_gemini_ocr"));
            
            return Result.ok(data);
        } catch (Exception e) {
            log.error("分析PDF失败", e);
            return Result.fail("分析PDF失败：" + e.getMessage());
        }
    }
    
    /**
     * 搜索相似内容
     */
    @RequestMapping("/search")
    public Result searchContent(String query, String chatId, @RequestParam(defaultValue = "3") int topK) {
        try {
            // 获取文件
            Resource file = fileRepository.getFile(chatId);
            if (!file.exists()) {
                return Result.fail("文件不存在");
            }
            
            // 使用本地PDF文件存储库搜索相似内容
            List<Document> results = localPdfFileRepository.searchSimilarContent(query, topK);
            
            // 过滤当前文件名的结果
            String filename = file.getFilename();
            results = results.stream()
                    .filter(doc -> {
                        Map<String, Object> metadata = doc.getMetadata();
                        return metadata.containsKey("file_name") &&
                               metadata.get("file_name").equals(filename);
                    })
                    .toList();
            
            // 构建返回结果
            List<Map<String, Object>> data = results.stream()
                .map(doc -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", doc.getId());
                    item.put("text", doc.getText());
                    item.put("metadata", doc.getMetadata());
                    item.put("page", doc.getMetadata().getOrDefault("page_number", "未知"));
                    item.put("isGeminiOcr", doc.getMetadata().containsKey("is_gemini_ocr"));
                    return item;
                })
                .toList();
            
            return Result.ok(data);
        } catch (Exception e) {
            log.error("搜索内容失败", e);
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }
}