package com.marcopolo.hima01.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiOcrService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    
    // 注意：Spring AI Vertex Gemini API正在适配中
    // private final VertexAiGeminiChatClient chatClient;

    /**
     * 使用Gemini模型提取PDF图像内容
     * @param pdfFile PDF文件
     * @param filename 文件名
     * @param chatId 会话ID
     * @return 提取的文档列表
     */
    public List<Document> extractTextWithGemini(File pdfFile, String filename, String chatId) {
        List<Document> result = new ArrayList<>();
        try {
            // 将PDF转换为图像
            List<File> imageFiles = convertPdfToImages(pdfFile);
            
            // 对每个图像使用Gemini进行OCR识别
            for (int i = 0; i < imageFiles.size(); i++) {
                final int pageIndex = i;
                File imageFile = imageFiles.get(i);
                
                try {
                    log.info("开始处理第{}页图像 (共{}页)", pageIndex + 1, imageFiles.size());
                    
                    // 暂时使用简化的方法
                    // 后续将实现完整的图像识别功能
                    String text = "正在适配Spring AI Gemini API，暂时无法提供OCR服务";
                    
                    if (text != null && !text.trim().isEmpty()) {
                        // 打印原始识别结果的前500个字符
                        log.info("Gemini原始识别第{}页文本内容(前500字符)：{}", pageIndex + 1, 
                                text.substring(0, Math.min(text.length(), 500)));
                        
                        // 创建Document对象
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("file_name", filename);
                        metadata.put("chat_id", chatId);
                        metadata.put("page_number", String.valueOf(pageIndex + 1));
                        metadata.put("is_gemini_ocr", "true");
                        
                        Document doc = new Document(
                                UUID.randomUUID().toString(),
                                text,
                                metadata
                        );
                        
                        result.add(doc);
                        log.info("Gemini成功提取第{}页文本，文本长度: {} 字符", 
                                pageIndex + 1, text.length());
                    } else {
                        log.warn("Gemini无法从第{}页提取文本", pageIndex + 1);
                    }
                } catch (Exception e) {
                    log.error("Gemini处理第{}页时出错: {}", pageIndex + 1, e.getMessage());
                } finally {
                    // 删除临时图像文件
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
            }
            
            log.info("Gemini OCR处理完成: {}, 成功识别 {}/{} 页", 
                    filename, result.size(), imageFiles.size());
        } catch (Exception e) {
            log.error("Gemini OCR处理PDF失败: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 使用Spring AI的Gemini从图像提取文本
     * @param imageFile 图像文件
     * @return 提取的文本内容
     */
    private String extractTextFromImage(File imageFile) {
        try {
            // TODO: 适配Spring AI Gemini API
            // 暂时返回占位文本
            return "正在适配Spring AI Gemini API，暂时无法提供OCR服务";
        } catch (Exception e) {
            log.error("使用Gemini从图像提取文本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将PDF转换为图像
     * @param pdfFile PDF文件
     * @return 图像文件列表
     */
    private List<File> convertPdfToImages(File pdfFile) {
        List<File> imageFiles = new ArrayList<>();
        
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfFile))) {
            int numberOfPages = pdfDocument.getNumberOfPages();
            
            for (int i = 1; i <= numberOfPages; i++) {
                // 检查是否有文本内容
                SimpleTextExtractionStrategy textStrategy = new SimpleTextExtractionStrategy();
                PdfCanvasProcessor parser = new PdfCanvasProcessor(textStrategy);
                parser.processPageContent(pdfDocument.getPage(i));
                String pageText = textStrategy.getResultantText();
                
                // 如果页面已经有文本内容，我们可以跳过图像处理
                if (pageText != null && !pageText.trim().isEmpty()) {
                    log.info("页面 {} 已经有文本内容，跳过图像转换", i);
                    continue;
                }
                
                // 如果没有文本，将页面转换为图像
                BufferedImage image = new BufferedImage(1240, 1754, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.drawRect(0, 0, 1240, 1754);
                g2d.dispose();
                
                // 保存图像到临时文件
                File tempFile = File.createTempFile("pdf_page_" + i + "_", ".png");
                ImageIO.write(image, "png", new FileOutputStream(tempFile));
                imageFiles.add(tempFile);
                
                log.info("转换PDF第{}页为图像", i);
            }
            
            log.info("已将PDF转换为{}个图像文件", imageFiles.size());
        } catch (Exception e) {
            log.error("PDF转换为图像失败: {}", e.getMessage());
        }
        
        return imageFiles;
    }
} 