package org.hackathon.genon.domain.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.hackathon.genon.domain.question.dto.QuestionResponseDto;
import org.hackathon.genon.domain.question.service.QuestionAiService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionService sessionService;
    private final QuestionAiService questionAiService; // ★ 추가

    /**
     * 매칭 성사 시 양쪽 유저에게 MATCH_FOUND 메시지 전송
     */
    public void onMatchCreated(MatchResult result) {
        String roomId = result.getRoomId();
        String roomKey = "match:room:" + roomId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        Long member1 = toLong(ops.get(roomKey, "member1"));
        Long member2 = toLong(ops.get(roomKey, "member2"));

        if (member1 == null || member2 == null) {
            log.warn("[GAME] room 정보가 비정상입니다. roomId={}", roomId);
            return;
        }

        String msgTo1 = """
            {"type":"MATCH_FOUND","roomId":"%s","opponentId":%d}
            """.formatted(roomId, member2);

        String msgTo2 = """
            {"type":"MATCH_FOUND","roomId":"%s","opponentId":%d}
            """.formatted(roomId, member1);

        sessionService.sendTo(member1, msgTo1);
        sessionService.sendTo(member2, msgTo2);
    }

    /**
     * 수락/거절 처리
     */
    public void handleAccept(String roomId, Long memberId, boolean accept) {

        String roomKey = "match:room:" + roomId;
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        Long member1 = toLong(ops.get(roomKey, "member1"));
        Long member2 = toLong(ops.get(roomKey, "member2"));

        if (member1 == null || member2 == null) {
            log.warn("[GAME] handleAccept - room 정보 없음. roomId={}", roomId);
            return;
        }

        Long opponentId = memberId.equals(member1) ? member2 : member1;

        /**
         * 1. 거절
         */
        if (!accept) {
            String rejectMsg = """
                {"type":"MATCH_RESULT","roomId":"%s","result":"REJECTED","by":%d}
                """.formatted(roomId, memberId);

            sessionService.sendTo(memberId, rejectMsg);
            sessionService.sendTo(opponentId, rejectMsg);

            redisTemplate.delete(roomKey);
            return;
        }

        /**
         * 2. 수락한 경우 → 실시간 알림
         */
        String acceptMsg = """
            {"type":"ACCEPT_STATUS","roomId":"%s","by":%d,"status":"ACCEPT"}
            """.formatted(roomId, memberId);

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
        {"type":"MATCH_SUCCESS","roomId":"%s","opponentId":%d}
        """.formatted(roomId, member2);

            String successMsgFor2 = """
        {"type":"MATCH_SUCCESS","roomId":"%s","opponentId":%d}
        """.formatted(roomId, member1);

            sessionService.sendTo(member1, successMsgFor1);
            sessionService.sendTo(member2, successMsgFor2);

            // 3-2. 문제 생성
            List<QuestionResponseDto> questions =
                    questionAiService.generateAndReturnQuestionsWithOptions();

            // 3-3. GAME_START JSON 생성
            String gameStartJson = buildGameStartJson(roomId, questions);

            // 3-4. 양쪽에게 게임 시작 메시지 전송
            sessionService.sendTo(member1, gameStartJson);
            sessionService.sendTo(member2, gameStartJson);

            log.info("[GAME] 매칭 최종 성사 + 게임 시작! roomId={} {} <> {}",
                    roomId, member1, member2);
        }
    }

    /**
     * GAME_START JSON 생성
     */
    private String buildGameStartJson(String roomId, List<QuestionResponseDto> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"GAME_START\",");
        sb.append("\"roomId\":\"").append(roomId).append("\",");
        sb.append("\"questions\":[");

        for (int i = 0; i < questions.size(); i++) {
            QuestionResponseDto q = questions.get(i);

            sb.append("{")
                    .append("\"id\":").append(q.id()).append(",")
                    .append("\"category\":\"").append(q.category()).append("\",")
                    .append("\"content\":\"").append(q.content()).append("\",")
                    .append("\"explanation\":\"").append(q.explanation()).append("\",");

            sb.append("\"options\":[");
            for (int j = 0; j < q.options().size(); j++) {
                var op = q.options().get(j);
                sb.append("{")
                        .append("\"content\":\"").append(op.content()).append("\",")
                        .append("\"correct\":").append(op.correct())
                        .append("}");

                if (j < q.options().size() - 1) sb.append(",");
            }
            sb.append("]"); // options 끝

            sb.append("}"); // question 끝

            if (i < questions.size() - 1) sb.append(",");
        }

        sb.append("]}");
        return sb.toString();
    }


    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("지원하지 않는 숫자 타입: " + value.getClass());
    }
}
