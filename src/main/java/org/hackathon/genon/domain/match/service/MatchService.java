package org.hackathon.genon.domain.match.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.hackathon.genon.domain.quiz.repository.QuizRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QuizRepository quizRepository;

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
        Quiz quiz = quizRepository.save(Quiz.createOneToOne());
        Long quizId = quiz.getId();
        String quizKey = "match:room:" + quizId;

        redisTemplate.opsForHash().put(quizKey, "member1", memberId);
        redisTemplate.opsForHash().put(quizKey, "member2", opponentId);
        redisTemplate.opsForHash().put(quizKey, "accept:" + memberId, false);
        redisTemplate.opsForHash().put(quizKey, "accept:" + opponentId, false);

        log.info("[MATCH] room created {}: {} <-> {}", quizId, memberId, opponentId);

        return MatchResult.matched(quizId, opponentId);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("지원하지 않는 숫자 타입: " + value.getClass());
    }
}
