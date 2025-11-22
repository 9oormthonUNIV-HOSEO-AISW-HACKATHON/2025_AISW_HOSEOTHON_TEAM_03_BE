package org.hackathon.genon.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // WebSocket 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final QuizSocketHandler gameSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/game"이라는 주소로 WebSocket 연결 요청이 오면,
        // gameSocketHandler가 처리하도록 등록합니다.
        registry.addHandler(gameSocketHandler, "/quiz")
                .setAllowedOrigins("*"); // 모든 도메인에서 오는 요청을 허용 (CORS)
    }

}