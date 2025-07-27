package com.seeyon.A8ContractPost.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * HTTP请求工具类
 */
@Slf4j
@Component
public class HttpClientUtil {

    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int SOCKET_TIMEOUT = 30000; // 30秒
    private static final String CHARSET = "UTF-8";

    /**
     * 创建支持HTTPS的HttpClient
     */
    private CloseableHttpClient createHttpClient() {
        try {
            // 创建SSL上下文，信任所有证书
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            
            // 创建SSL连接工厂
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"},
                    null,
                    NoopHostnameVerifier.INSTANCE);
            
            // 配置请求超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build();
            
            // 创建HttpClient
            return HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("创建HttpClient失败", e);
            throw new RuntimeException("创建HttpClient失败", e);
        }
    }

    /**
     * 发送GET请求
     *
     * @param url    请求URL
     * @param params 请求参数
     * @return 响应内容
     */
    public String doGet(String url, Map<String, String> params) {
        // 构建带参数的URL
        StringBuilder urlBuilder = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> {
                urlBuilder.append(key).append("=").append(value).append("&");
            });
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        HttpGet httpGet = new HttpGet(urlBuilder.toString());
        httpGet.setHeader("Content-Type", "application/json;charset=UTF-8");

        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(httpGet)) {
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, CHARSET);
                log.debug("GET请求响应: {}", result);
                return result;
            }
        } catch (IOException e) {
            log.error("GET请求异常: {}", url, e);
            throw new RuntimeException("GET请求异常", e);
        }
        
        return null;
    }

    /**
     * 发送POST请求
     *
     * @param url     请求URL
     * @param jsonStr 请求JSON字符串
     * @return 响应内容
     */
    public String doPost(String url, String jsonStr) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        
        if (jsonStr != null) {
            StringEntity entity = new StringEntity(jsonStr, CHARSET);
            entity.setContentEncoding(CHARSET);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
        }
        
        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, CHARSET);
                log.debug("POST请求响应: {}", result);
                return result;
            }
        } catch (IOException e) {
            log.error("POST请求异常: {}", url, e);
            throw new RuntimeException("POST请求异常", e);
        }
        
        return null;
    }
} 