package org.hackathon.genon.global.websocket;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizSocketHandler extends TextWebSocketHandler {

    public static final String ACCESS_TOKEN_PREFIX = "accessToken=";
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final JwtProvider jwtProvider;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. URL 쿼리 파라미터에서 JWT 토큰을 추출합니다.
        String token = extractTokenFromQuery(session);

        // 2. 토큰이 유효한지 검증합니다.
        if (token == null || !jwtProvider.isValidateToken(token)) {
            log.warn("⚠️ 유효하지 않은 토큰으로 연결 시도, 연결을 종료합니다.");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid Token"));
            return;
        }

        // 3. 토큰에서 userId를 추출합니다.
        Long memberId = jwtProvider.getMemberIdFromToken(token);

        // 4. 세션을 맵에 저장합니다. (이제 서버는 누가 연결했는지 알 수 있습니다)
        session.getAttributes().put("memberId", memberId); // 세션 속성에 userId '이름표' 붙이기
        sessions.put(memberId, session);

        log.info("✅ WebSocket 연결 성공: memberId={}, sessionId={}", memberId, session.getId());
    }

    // [헬퍼 메서드] URL 쿼리에서 토큰을 추출하는 기능
    private String extractTokenFromQuery(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery(); // "token=eyJ..."
        if (query != null && query.startsWith(ACCESS_TOKEN_PREFIX)) {
            return query.substring(ACCESS_TOKEN_PREFIX.length()); // "eyJ..."
        }
        return null;
    }

}