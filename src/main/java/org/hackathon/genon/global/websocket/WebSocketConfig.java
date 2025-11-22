package org.hackathon.genon.global.websocket;

import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.global.security.jwt.JwtProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket // WebSocket 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final QuizSocketHandler gameSocketHandler;
    private final JwtProvider jwtProvider;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameSocketHandler, "/quiz")
                .addInterceptors(new HttpSessionHandshakeInterceptor(){
                    @Override
                    public boolean beforeHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) {

                        // accessToken=? 파싱
                        String query = request.getURI().getQuery();
                        String token = query.replace("accessToken=", "");

                        Long memberId = jwtProvider.getMemberIdFromToken(token);
                        attributes.put("memberId", memberId);

                        return true;
                    }
                })
                .setAllowedOrigins("*");
    }



}