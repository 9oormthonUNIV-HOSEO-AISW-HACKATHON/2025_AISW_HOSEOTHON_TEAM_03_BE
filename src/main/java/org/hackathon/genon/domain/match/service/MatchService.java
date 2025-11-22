package org.hackathon.genon.domain.match.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final RedisTemplate<String, Object> redisTemplate;

    public MatchResult joinMatch(Long memberId, GenerationRole generationRole) {

        String myQueueKey = (generationRole == GenerationRole.MZ)
                ? "queue:MZ"
                : "queue:SENIOR";

        String oppQueueKey = (generationRole == GenerationRole.MZ)
                ? "queue:SENIOR"
                : "queue:MZ";

        // 상대 찾기
        Object popped = redisTemplate.opsForList().leftPop(oppQueueKey);
        Long opponentId = toLong(popped);   // ★ 수정 포인트

        if (opponentId == null) {
            // 대기
            redisTemplate.opsForList().rightPush(myQueueKey, memberId);
            log.info("[MATCH] {} added to queue {}", memberId, myQueueKey);
            return MatchResult.waiting();
        }

        // 매칭 성사 → 방 생성
        String roomId = UUID.randomUUID().toString();
        String roomKey = "match:room:" + roomId;

        redisTemplate.opsForHash().put(roomKey, "member1", memberId);
        redisTemplate.opsForHash().put(roomKey, "member2", opponentId);
        redisTemplate.opsForHash().put(roomKey, "accept:" + memberId, false);
        redisTemplate.opsForHash().put(roomKey, "accept:" + opponentId, false);

        log.info("[MATCH] room created {}: {} <-> {}", roomId, memberId, opponentId);

        return MatchResult.matched(roomId, opponentId);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("지원하지 않는 숫자 타입: " + value.getClass());
    }
}
