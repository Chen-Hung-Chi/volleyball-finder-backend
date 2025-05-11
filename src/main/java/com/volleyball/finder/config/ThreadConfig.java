package com.volleyball.finder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 提供虛擬執行緒 Executor，Spring 關閉時自動釋放資源
 */
@Configuration
public class ThreadConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService notificationExecutor() {
        // Java21 虛擬執行緒：每個任務 1 條
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}