package com.volleyball.finder.config;


import com.volleyball.finder.util.AESUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class AesConfig {

    @Value("${aes.key}")
    private String base64Key;

    @Bean
    public AESUtil aesUtil() {
        byte[] rawKey = Base64.getDecoder().decode(base64Key);
        return new AESUtil(rawKey);
    }
}