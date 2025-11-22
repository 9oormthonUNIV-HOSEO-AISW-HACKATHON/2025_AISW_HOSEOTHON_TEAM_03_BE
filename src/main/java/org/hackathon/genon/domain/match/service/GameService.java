package org.hackathon.genon.domain.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionService sessionService; // WebSocket으로 메시지 보내는 역할

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

        // 양쪽에게 "수락/거절 선택하세요"
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

        /*
         * 1. 거절한 경우: 즉시 MATCH_RESULT:REJECTED 전송
         */
        if (!accept) {
            String rejectMsg = """
                {"type":"MATCH_RESULT","roomId":"%s","result":"REJECTED","by":%d}
                """.formatted(roomId, memberId);

            sessionService.sendTo(memberId, rejectMsg);
            sessionService.sendTo(opponentId, rejectMsg);

            redisTemplate.delete(roomKey);
            log.info("[GAME] {} 가 매칭 거절 -> 방 {} 삭제", memberId, roomId);
            return;
        }

        /*
         * 2. 수락한 경우: accept 상태 기록
         */
        ops.put(roomKey, "accept:" + memberId, true);

        boolean member1Accepted = Boolean.TRUE.equals(ops.get(roomKey, "accept:" + member1));
        boolean member2Accepted = Boolean.TRUE.equals(ops.get(roomKey, "accept:" + member2));

        /*
         * 3. 둘 다 수락 → MATCH_SUCCESS 전송
         */
        if (member1Accepted && member2Accepted) {

            String success1 = """
                {"type":"MATCH_SUCCESS","roomId":"%s","opponentId":%d}
                """.formatted(roomId, member2);

            String success2 = """
                {"type":"MATCH_SUCCESS","roomId":"%s","opponentId":%d}
                """.formatted(roomId, member1);

            sessionService.sendTo(member1, success1);
            sessionService.sendTo(member2, success2);

            log.info("[GAME] roomId={} 매칭 최종 성사! {} <> {}", roomId, member1, member2);

            // 여기서 게임 시작 로직으로 이동 가능

            // 매칭 성공 후 방 삭제 여부는 선택
            // redisTemplate.delete(roomKey);
        }
    }

    /**
     * Redis 숫자 타입 변환 안전하게 처리
     */
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);

        throw new IllegalArgumentException("지원하지 않는 숫자 타입: " + value.getClass());
    }
}
