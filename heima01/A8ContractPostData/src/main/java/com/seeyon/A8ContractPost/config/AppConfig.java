package com.seeyon.A8ContractPost.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 应用配置类
 */
@Configuration
public class AppConfig {

    /**
     * OA系统数据源配置
     */
    @Bean(name = "oaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.oa")
    @Primary
    public DataSource oaDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * OA系统JdbcTemplate配置
     */
    @Bean(name = "oaJdbcTemplate")
    @Primary
    public JdbcTemplate oaJdbcTemplate(@Qualifier("oaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 本地数据源配置（用于存储缓存数据和日志）
     */
    @Bean(name = "localDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.local")
    public DataSource localDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 本地JdbcTemplate配置
     */
    @Bean(name = "localJdbcTemplate")
    public JdbcTemplate localJdbcTemplate(@Qualifier("localDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
} 