package com.volleyball.finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class VolleyballFinderApplication {
    public static void main(String[] args) {
        SpringApplication.run(VolleyballFinderApplication.class, args);
    }
} 