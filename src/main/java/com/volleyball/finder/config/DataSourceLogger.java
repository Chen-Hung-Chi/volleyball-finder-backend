package com.volleyball.finder.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DataSourceLogger {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @PostConstruct
    public void logDatasourceUrl() {
        log.info("spring.datasource.url = {}", datasourceUrl);
    }
}