package com.volleyball.finder.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    // 改為支援 Base64；沿用同一個 key，直接放 Base64 字串即可
    @Value("${firebase.service-account}")
    private String serviceAccountValue;

    @PostConstruct
    public void init() throws IOException {
        if (serviceAccountValue == null || serviceAccountValue.isEmpty()) {
            throw new IllegalStateException("Missing firebase.service-account configuration");
        }

        String json = serviceAccountValue.trim();

        // 嘗試以 Base64 解碼；若不是合法 Base64 且是以 { 開頭，視為原始 JSON
        try {
            byte[] decoded = Base64.getDecoder().decode(json);
            json = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignore) {
            // 不是 Base64，可能是原始 JSON
            if (!json.startsWith("{")) {
                throw new IllegalStateException("firebase.service-account must be Base64 of the service account JSON or a raw JSON string");
            }
        }

        // 若 private_key 內是 \n（單行環境變數常見），轉回真正換行，避免 PEM 解析失敗
        if (json.contains("\\n") && json.contains("PRIVATE KEY")) {
            json = json.replace("\\n", "\n");
        }

        ByteArrayInputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}