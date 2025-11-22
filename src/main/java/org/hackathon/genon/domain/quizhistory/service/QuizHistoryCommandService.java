package org.hackathon.genon.domain.quizhistory.service;

import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.hackathon.genon.domain.quiz.repository.QuizRepository;
import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.hackathon.genon.domain.quizhistory.entity.QuizResult;
import org.hackathon.genon.domain.quizhistory.repository.QuizHistoryRepository;
import org.hackathon.genon.global.error.CoreException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuizHistoryCommandService {

    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;
    private final QuizHistoryRepository quizHistoryRepository;

    /**
     * 하나의 퀴즈(게임)에 대한 2명의 최종 결과 저장
     */
    // quizId별 락용 객체 캐시
    private final ConcurrentHashMap<Long, Object> quizLocks = new ConcurrentHashMap<>();

    @Transactional
    public void recordFinalResult(Long quizId,
                                  Long member1Id, Long member2Id,
                                  int score1, int score2) {

        Object lock = quizLocks.computeIfAbsent(quizId, id -> new Object());

        synchronized (lock) {
            try {
                // 이미 저장된 적 있으면 또 저장 / 포인트 반영 안 함 (중복 방지)
                if (!quizHistoryRepository.findByQuizId(quizId).isEmpty()) {
                    log.warn("[QUIZ_HISTORY] quizId={} 는 이미 저장됨. 중복 호출 무시", quizId);
                    return;
                }

                // ======= 기존 로직 그대로 =======
                Quiz quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new CoreException("퀴즈가 존재하지 않습니다. id=" + quizId));

                Member member1 = memberRepository.findById(member1Id)
                        .orElseThrow(() -> new CoreException("회원이 존재하지 않습니다. id=" + member1Id));

                Member member2 = memberRepository.findById(member2Id)
                        .orElseThrow(() -> new CoreException("회원이 존재하지 않습니다. id=" + member2Id));

                QuizResult result1;
                QuizResult result2;

                if (score1 > score2) {
                    result1 = QuizResult.WIN;
                    result2 = QuizResult.LOSE;
                } else if (score2 > score1) {
                    result1 = QuizResult.LOSE;
                    result2 = QuizResult.WIN;
                } else {
                    result1 = QuizResult.DRAW;
                    result2 = QuizResult.DRAW;
                }

                int delta1 = calculatePointDelta(result1);
                int delta2 = calculatePointDelta(result2);

                QuizHistory history1 = QuizHistory.create(quiz, member1, score1, result1, delta1);
                QuizHistory history2 = QuizHistory.create(quiz, member2, score2, result2, delta2);

                quizHistoryRepository.save(history1);
                quizHistoryRepository.save(history2);

                applyPointChange(member1, delta1);
                applyPointChange(member2, delta2);

                log.info("[QUIZ_HISTORY] quizId={} 최종 결과 저장 완료. member1={}, member2={}",
                        quizId, member1Id, member2Id);
            } finally {
                quizLocks.remove(quizId);
            }
        }
    }


    private int calculatePointDelta(QuizResult result) {
        // 필요하면 값 바꿔도 됨
        return switch (result) {
            case WIN -> 50;
            case LOSE -> -5;
            case DRAW -> 0;
        };
    }

    private void applyPointChange(Member member, int delta) {
        if (delta > 0) {
            member.increasePoints((long) delta);
        } else if (delta < 0) {
            member.decreasePoints((long) (-delta)); // 음수 보정
        }
        // 점수, 등급 변경은 Member 내부에서 처리됨
    }

}
