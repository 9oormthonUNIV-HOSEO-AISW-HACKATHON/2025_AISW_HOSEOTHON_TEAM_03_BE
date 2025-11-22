package org.hackathon.genon.domain.quizhistory.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class QuizHistoryCommandService {

    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;
    private final QuizHistoryRepository quizHistoryRepository;

    /**
     * 하나의 퀴즈(게임)에 대한 2명의 최종 결과 저장
     */
    @Transactional
    public void recordFinalResult(Long quizId,
                                  Long member1Id, Long member2Id,
                                  int score1, int score2) {

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

        //  포인트 보상 규칙
        int delta1 = calculatePointDelta(result1);
        int delta2 = calculatePointDelta(result2);

        //  QuizHistory 저장 (2줄)
        QuizHistory history1 = QuizHistory.create(quiz, member1, score1, result1, delta1);
        QuizHistory history2 = QuizHistory.create(quiz, member2, score2, result2, delta2);

        quizHistoryRepository.save(history1);
        quizHistoryRepository.save(history2);

        //  Member 포인트 반영
        applyPointChange(member1, delta1);
        applyPointChange(member2, delta2);
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
