package org.hackathon.genon.domain.match.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.hackathon.genon.domain.question.dto.QuestionResponse;
import org.hackathon.genon.domain.question.service.QuestionAiService;
import org.hackathon.genon.global.error.CoreException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionService sessionService;
    private final QuestionAiService questionAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 매칭 성사 시 양쪽 유저에게 MATCH_FOUND 메시지 전송
     */
    public void onMatchCreated(MatchResult result) {
        Long quizId = result.getQuizId();
        String quizKey = "match:room:" + quizId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        Long member1 = toLong(ops.get(quizKey, "member1"));
        Long member2 = toLong(ops.get(quizKey, "member2"));

        if (member1 == null || member2 == null) {
            log.warn("[GAME] room 정보가 비정상입니다. quizId={}", quizId);
            return;
        }

        String msgTo1 = """
            {"type":"MATCH_FOUND","quizId":"%s","opponentId":%d}
            """.formatted(quizId, member2);

        String msgTo2 = """
            {"type":"MATCH_FOUND","quizId":"%s","opponentId":%d}
            """.formatted(quizId, member1);

        sessionService.sendTo(member1, msgTo1);
        sessionService.sendTo(member2, msgTo2);
    }

    /**
     * 수락/거절 처리
     */
    public void handleAccept(Long quizId, Long memberId, boolean accept) {

        String roomKey = "match:room:" + quizId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        Long member1 = toLong(ops.get(roomKey, "member1"));
        Long member2 = toLong(ops.get(roomKey, "member2"));

        if (member1 == null || member2 == null) {
            log.warn("[GAME] handleAccept - room 정보 없음. quizId={}", quizId);
            return;
        }

        Long opponentId = memberId.equals(member1) ? member2 : member1;

        /**
         * 1. 거절
         */
        if (!accept) {
            String rejectMsg = """
                {"type":"MATCH_RESULT","quizId":"%s","result":"REJECTED","by":%d}
                """.formatted(quizId, memberId);

            sessionService.sendTo(memberId, rejectMsg);
            sessionService.sendTo(opponentId, rejectMsg);

            redisTemplate.delete(roomKey);
            return;
        }

        /**
         * 2. 수락한 경우 → 실시간 알림
         */
        String acceptMsg = """
            {"type":"ACCEPT_STATUS","quizId":"%s","by":%d,"status":"ACCEPT"}
            """.formatted(quizId, memberId);

        sessionService.sendTo(memberId, acceptMsg);
        sessionService.sendTo(opponentId, acceptMsg);

        ops.put(roomKey, "accept:" + memberId, true);

        boolean member1Accepted = Boolean.TRUE.equals(ops.get(roomKey, "accept:" + member1));
        boolean member2Accepted = Boolean.TRUE.equals(ops.get(roomKey, "accept:" + member2));

        /**
         * 3. 둘 다 수락 → 매칭 성공 → 매칭 성사 메시지 + 문제 생성 + 전송
         */
        if (member1Accepted && member2Accepted) {

            // 3-1. 매칭 성사 알림 먼저 보내기
            String successMsgFor1 = """
        {"type":"MATCH_SUCCESS","quizId":"%s","opponentId":%d}
        """.formatted(quizId, member2);

            String successMsgFor2 = """
        {"type":"MATCH_SUCCESS","quizId":"%s","opponentId":%d}
        """.formatted(quizId, member1);

            sessionService.sendTo(member1, successMsgFor1);
            sessionService.sendTo(member2, successMsgFor2);

            // 3-2. 문제 생성
            List<QuestionResponse> questions =
                    questionAiService.generateAndReturnQuestionsWithOptions(quizId);

            // 3-3. GAME_START JSON 생성
            String gameStartJson = buildGameStartJson(quizId, questions);

            // 3-4. 양쪽에게 게임 시작 메시지 전송
            sessionService.sendTo(member1, gameStartJson);
            sessionService.sendTo(member2, gameStartJson);

            log.info("[GAME] 매칭 최종 성사 + 게임 시작! quizId={} {} <> {}",
                    quizId, member1, member2);
        }
    }

    /**
     * GAME_START JSON 생성
     */

    private String buildGameStartJson(Long quizId, List<QuestionResponse> questions) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "GAME_START");
        root.put("quizId", quizId);

        ArrayNode questionsArray = objectMapper.createArrayNode();

        for (QuestionResponse q : questions) {
            ObjectNode questionNode = objectMapper.createObjectNode();
            questionNode.put("id", q.id());
            questionNode.put("category", q.category());
            questionNode.put("content", q.content());
            questionNode.put("explanation", q.explanation());

            ArrayNode optionsArray = objectMapper.createArrayNode();
            for (var op : q.options()) {
                ObjectNode optionNode = objectMapper.createObjectNode();
                optionNode.put("content", op.content());
                //optionNode.put("correct", op.correct());
                optionsArray.add(optionNode);
            }

            questionNode.set("options", optionsArray);
            questionsArray.add(questionNode);
        }

        root.set("questions", questionsArray);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            // 예외 처리 또는 로그 추가
            throw new CoreException("JSON 변환 중 오류 발생");
        }
    }


    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("지원하지 않는 숫자 타입: " + value.getClass());
    }
}
