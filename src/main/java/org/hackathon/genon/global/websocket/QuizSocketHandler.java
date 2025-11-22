package org.hackathon.genon.global.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
import org.hackathon.genon.domain.question.entity.Question;
import org.hackathon.genon.domain.question.repository.QuestionRepository;
import org.hackathon.genon.domain.quiz.repository.QuizOptionRepository;
import org.hackathon.genon.domain.quizoption.entity.QuizOption;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.security.jwt.JwtProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final QuestionRepository questionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final MatchService matchService;
    private final GameService gameService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // ==========================
    //  연결 처리
    // ==========================
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractTokenFromQuery(session);

        if (token == null || !jwtProvider.isValidateToken(token)) {
            log.warn("⚠️ 유효하지 않은 토큰으로 연결 시도, 연결 종료");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid Token"));
            return;
        }

        Long memberId = jwtProvider.getMemberIdFromToken(token);

        sessionService.register(memberId, session);
        session.getAttributes().put("memberId", memberId);

        log.info("✅ WebSocket 연결 성공: memberId={}, sessionId={}", memberId, session.getId());
    }

    private String extractTokenFromQuery(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery(); // "accessToken=eyJ..."
        if (query != null && query.startsWith(ACCESS_TOKEN_PREFIX)) {
            return query.substring(ACCESS_TOKEN_PREFIX.length());
        }
        return null;
    }

    // ==========================
    //  메시지 처리
    // ==========================
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
            case "ANSWER_SUBMIT" -> handleAnswerSubmit(root, memberId);
            default -> {
                log.warn("알 수 없는 type: {}", type);
                session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"UNKNOWN_TYPE\"}"));
            }
        }
    }

    // ==========================
    //  MATCH_JOIN
    // ==========================
    private void handleMatchJoin(WebSocketSession session, Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException("회원이 존재하지 않습니다. id=" + memberId));

        GenerationRole generationRole = member.getGenerationRole();

        MatchResult result = matchService.joinMatch(memberId, generationRole);

        String selfJson = """
                {
                  "type":"MATCH_JOIN_RESULT",
                  "matched":%s,
                  "quizId":%s,
                  "opponentId":%s
                }
                """.formatted(
                result.isMatched(),
                result.getQuizId() == null ? "null" : "\"" + result.getQuizId() + "\"",
                result.getOpponentId() == null ? "null" : result.getOpponentId()
        );
        session.sendMessage(new TextMessage(selfJson));

        if (result.getQuizId() != null) {
            gameService.onMatchCreated(result);
        }

        log.info("[WS] MATCH_JOIN 처리 완료 memberId={}, matched={}, quizId={}",
                memberId, result.isMatched(), result.getQuizId());
    }

    // ==========================
    //  MATCH_ACCEPT
    // ==========================
    private void handleMatchAccept(JsonNode root, Long memberId) {
        Long quizId = root.path("quizId").asLong();
        boolean accept = root.path("accept").asBoolean(false);

        if (quizId == null) {
            log.warn("MATCH_ACCEPT 에 quizId 없음");
            return;
        }

        gameService.handleAccept(quizId, memberId, accept);
        log.info("[WS] MATCH_ACCEPT 처리 완료 memberId={}, quizId={}, accept={}", memberId, quizId, accept);
    }

    // ==========================
    //  ANSWER_SUBMIT 진입점
    // ==========================
    private void handleAnswerSubmit(JsonNode root, Long memberId) {
        String roomId = root.path("quizId").asText(null);
        Long questionId = root.path("questionId").asLong();
        int answerIndex = root.path("answerIndex").asInt(-1);   // 기본값 -1 → 오답 처리

        if (roomId == null) {
            log.warn("ANSWER_SUBMIT 에 quizId 없음");
            return;
        }

        handleAnswer(roomId, memberId, questionId, answerIndex);

        log.info("[WS] ANSWER_SUBMIT 처리 완료 memberId={}, quizId={}, questionId={}, answerIndex={}",
                memberId, roomId, questionId, answerIndex);
    }

    // ==========================
    //  실제 정답 검증 + 점수 계산
    // ==========================
    public void handleAnswer(String roomId, Long memberId, Long questionId, int answerIndex) {
        String roomKey = "match:room:" + roomId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        Long member1 = toLong(ops.get(roomKey, "member1"));
        Long member2 = toLong(ops.get(roomKey, "member2"));

        if (member1 == null || member2 == null) return;
        if (!memberId.equals(member1) && !memberId.equals(member2)) return;

        Long opponentId = memberId.equals(member1) ? member2 : member1;

        // ----------------------
        // ① 정답 검증 로직 (그대로)
        // ----------------------
        boolean isCorrect = false;

        try {
            if (answerIndex >= 0) {
                Question question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new IllegalArgumentException("문제 없음"));

                List<QuizOption> options =
                        quizOptionRepository.findByQuestionIdOrderByIdAsc(question.getId());

                if (answerIndex < options.size()) {
                    isCorrect = options.get(answerIndex).isCorrect();
                }
            }
        } catch (Exception e) {
            isCorrect = false;
        }

        // ----------------------
        // ② 점수 갱신 (그대로)
        // ----------------------
        Long score1 = toLong(ops.get(roomKey, "score:" + member1));
        Long score2 = toLong(ops.get(roomKey, "score:" + member2));
        if (score1 == null) score1 = 0L;
        if (score2 == null) score2 = 0L;

        if (isCorrect) {
            if (memberId.equals(member1)) {
                score1 = ops.increment(roomKey, "score:" + member1, 1L);
            } else {
                score2 = ops.increment(roomKey, "score:" + member2, 1L);
            }
        }

        // ----------------------
        // ③ 진행도 업데이트 로직 (단순히 +1만)
        // ----------------------
        Long totalQuestions = toLong(ops.get(roomKey, "totalQuestions"));
        if (totalQuestions == null) totalQuestions = 5L;

        Long myProgress = toLong(ops.get(roomKey, "progress:" + memberId));
        if (myProgress == null) myProgress = 0L;
        myProgress = ops.increment(roomKey, "progress:" + memberId, 1L);

        Long oppProgress = toLong(ops.get(roomKey, "progress:" + opponentId));
        if (oppProgress == null) oppProgress = 0L;

        // ----------------------
        // 🔥 ④ 마지막 사람이 마지막 문제까지 풀었는지 체크
        // ----------------------
        boolean isLastAnswer =
                myProgress >= totalQuestions && oppProgress >= totalQuestions;

        String eventType = isLastAnswer ? "ANSWER_DONE" : "ANSWER_RESULT";

        // ----------------------
        // ⑤ JSON 생성 및 전송
        // ----------------------
        String answerJson = """
        {
          "type": "%s",
          "quizId": "%s",
          "questionId": %d,
          "answeredBy": %d,
          "correct": %s,
          "score": {
            "member1": %d,
            "member2": %d
          }
        }
        """.formatted(
                eventType, roomId, questionId, memberId,
                isCorrect, score1, score2
        );

        sessionService.sendTo(member1, answerJson);
        sessionService.sendTo(member2, answerJson);
    }


    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("지원하지 않는 숫자 타입: " + value.getClass());
    }

    // ==========================
    //  연결 종료
    // ==========================
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long memberId = (Long) session.getAttributes().get("memberId");
        if (memberId != null) {
            sessionService.remove(memberId);
            log.info("🔌 WebSocket 종료: memberId={}, reason={}", memberId, status);
        }
    }
}
