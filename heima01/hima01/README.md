# 智能PDF聊天应用

这是一个基于Spring Boot和Spring AI的智能聊天应用，支持用户上传PDF文件并与其内容进行对话。
本应用使用OpenAI的大语言模型及Milvus向量数据库来实现智能问答系统。

## 功能特性

- PDF文件上传和管理
- 文本抽取和向量化存储
- 基于语义搜索的智能问答
- 支持扫描PDF（图像形式）的OCR文本识别
- 聊天历史记录保存

## 系统要求

- Java 17或更高版本
- Maven 3.6+
- MySQL数据库
- Milvus向量数据库
- Tesseract OCR（用于处理扫描PDF）

## 安装与配置

### 1. 安装Tesseract OCR

#### Windows系统:

1. 下载并安装Tesseract OCR：https://github.com/UB-Mannheim/tesseract/wiki
2. 安装时请选择包含中文和英文语言包
3. 添加环境变量`TESSDATA_PREFIX`，指向tessdata文件夹，例如：`C:\Program Files\Tesseract-OCR\tessdata`

#### Linux系统:

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install tesseract-ocr
sudo apt install tesseract-ocr-eng tesseract-ocr-chi-sim

# CentOS/RHEL
sudo yum install tesseract
sudo yum install tesseract-langpack-eng tesseract-langpack-chi_sim
```

#### Mac系统:

```bash
brew install tesseract
brew install tesseract-lang    # 安装所有语言包，或者指定语言
```

### 2. 验证Tesseract安装

在命令行中运行以下命令验证Tesseract是否安装成功：

```bash
tesseract --version
tesseract --list-langs  # 确认已安装中文(chi_sim)和英文(eng)语言包
```

### 3. 配置应用

1. 克隆代码库
2. 使用Maven构建项目：`mvn clean package`
3. 配置数据库连接和Milvus连接参数

## 使用说明

1. 启动应用：`java -jar target/hima01-0.0.1-SNAPSHOT.jar`
2. 上传PDF文件（支持标准PDF和扫描PDF）
3. 开始与PDF内容对话

## 技术架构

- **Spring Boot**: 应用框架
- **Spring AI**: 提供AI集成能力
- **OpenAI**: 提供大语言模型
- **Milvus**: 向量数据库，存储文档向量
- **PDFBox**: PDF解析
- **Tesseract OCR**: 扫描PDF文本识别

## 扫描PDF处理流程

对于扫描格式的PDF文件（实质上是图像），应用采用以下处理流程：

1. 检测PDF是否为扫描格式（无文本内容）
2. 将PDF页面渲染为高分辨率图像（300dpi）
3. 使用Tesseract OCR引擎识别图像中的文本
4. 对OCR结果进行清理和优化
5. 将识别结果创建为Document对象并添加元数据
6. 存入Milvus向量数据库用于后续检索

## 常见问题

### OCR识别不准确
- 确保安装了最新版本的Tesseract OCR
- 验证已安装正确的语言包（中文需要chi_sim）
- 对于复杂版面的文档，OCR识别可能不完美

### 应用启动失败
- 检查Tesseract OCR是否正确安装
- 验证环境变量`TESSDATA_PREFIX`是否正确设置
- 确保Milvus向量数据库已启动并可连接 