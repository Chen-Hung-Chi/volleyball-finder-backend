package com.volleyball.finder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // 這裡改成 pattern 支援更廣泛的前端網域（包含 port）
                .setAllowedOriginPatterns(frontendUrl); // or frontendUrl if fully trusted
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // client 發送訊息用的 prefix，例如：stomp.send("/app/...")
        registry.setApplicationDestinationPrefixes("/app");

        // server 廣播回 client 用的路徑 prefix
        registry.enableSimpleBroker("/topic", "/queue");

        // 若要支援 /user/xxx 推送（如私人通知），需要加上這行
        registry.setUserDestinationPrefix("/user");
    }
}