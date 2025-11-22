package org.hackathon.genon.global.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.hackathon.genon.domain.match.service.GameService;
import org.hackathon.genon.domain.match.service.MatchService;
import org.hackathon.genon.domain.match.service.SessionService;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizSocketHandler extends TextWebSocketHandler {

    public static final String ACCESS_TOKEN_PREFIX = "accessToken=";

    private final JwtProvider jwtProvider;
    private final SessionService sessionService;

    // ★ WebSocket으로 매칭 요청/수락을 처리하기 위해 추가 의존성
    private final MatchService matchService;
    private final GameService gameService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. URL 쿼리 파라미터에서 JWT 토큰 추출
        String token = extractTokenFromQuery(session);

        // 2. 토큰 검증
        if (token == null || !jwtProvider.isValidateToken(token)) {
            log.warn("⚠️ 유효하지 않은 토큰으로 연결 시도, 연결을 종료합니다.");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid Token"));
            return;
        }

        // 3. 토큰에서 memberId 추출
        Long memberId = jwtProvider.getMemberIdFromToken(token);

        // 4. 세션 등록 및 속성에 memberId 저장
        sessionService.register(memberId, session);
        session.getAttributes().put("memberId", memberId);

        log.info("✅ WebSocket 연결 성공: memberId={}, sessionId={}", memberId, session.getId());
    }

    // [헬퍼 메서드] URL 쿼리에서 토큰 추출
    private String extractTokenFromQuery(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery(); // "accessToken=eyJ..."
        if (query != null && query.startsWith(ACCESS_TOKEN_PREFIX)) {
            return query.substring(ACCESS_TOKEN_PREFIX.length()); // "eyJ..."
        }
        return null;
    }

    /**
     * 클라이언트가 보내는 WebSocket 메시지 처리
     * - MATCH_JOIN   : 매칭 참여
     * - MATCH_ACCEPT : 매칭 수락/거절
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("📥 WS 메시지 수신: {}", payload);

        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (Exception e) {
            log.warn("잘못된 JSON 메시지: {}", payload, e);
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"Invalid JSON\"}"));
            return;
        }

        String type = root.path("type").asText(null);
        Long memberId = (Long) session.getAttributes().get("memberId");

        if (memberId == null) {
            log.warn("memberId 없는 세션에서 메시지 수신 - 무시");
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"UNAUTHORIZED\"}"));
            return;
        }

        if (type == null) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"TYPE_REQUIRED\"}"));
            return;
        }

        switch (type) {
            case "MATCH_JOIN" -> handleMatchJoin(session, memberId);
            case "MATCH_ACCEPT" -> handleMatchAccept(root, memberId);
            default -> {
                log.warn("알 수 없는 type: {}", type);
                session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"UNKNOWN_TYPE\"}"));
            }
        }
    }

    /**
     * MATCH_JOIN 처리: 큐에 넣거나, 상대가 있으면 room 생성 후 MATCH_FOUND 실시간 전파
     */
    private void handleMatchJoin(WebSocketSession session, Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. id=" + memberId));

        GenerationRole generationRole = member.getGenerationRole();

        MatchResult result = matchService.joinMatch(memberId, generationRole);

        // 내게는 현재 상태를 알려주는 응답 하나 보내주고
        String selfJson = """
                {
                  "type":"MATCH_JOIN_RESULT",
                  "matched":%s,
                  "roomId":%s,
                  "opponentId":%s
                }
                """.formatted(
                result.isMatched(),
                result.getRoomId() == null ? "null" : "\"" + result.getRoomId() + "\"",
                result.getOpponentId() == null ? "null" : result.getOpponentId()
        );
        session.sendMessage(new TextMessage(selfJson));

        // 방이 생성된 경우 → 양쪽에게 MATCH_FOUND + 이후 ACCEPT 로직은 GameService가 처리
        if (result.getRoomId() != null) {
            gameService.onMatchCreated(result);
        }

        log.info("[WS] MATCH_JOIN 처리 완료 memberId={}, matched={}, roomId={}",
                memberId, result.isMatched(), result.getRoomId());
    }

    /**
     * MATCH_ACCEPT 처리: GameService.handleAccept 호출
     */
    private void handleMatchAccept(JsonNode root, Long memberId) {
        String roomId = root.path("roomId").asText(null);
        boolean accept = root.path("accept").asBoolean(false);

        if (roomId == null) {
            log.warn("MATCH_ACCEPT 에 roomId 없음");
            return;
        }

        gameService.handleAccept(roomId, memberId, accept);
        log.info("[WS] MATCH_ACCEPT 처리 완료 memberId={}, roomId={}, accept={}", memberId, roomId, accept);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long memberId = (Long) session.getAttributes().get("memberId");
        if (memberId != null) {
            sessionService.remove(memberId);
            log.info("🔌 WebSocket 연결 종료: memberId={}, reason={}", memberId, status);
        }
    }
}
