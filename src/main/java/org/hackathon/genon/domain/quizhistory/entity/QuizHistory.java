package org.hackathon.genon.domain.quizhistory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 특정 게임에 대한 각 개인의 최종 성적과 결과, 포인트 변동을 기록합니다.
 */
@Entity
@Table(name = "quiz_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "score", nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10, nullable = false)
    private QuizResult result; // WIN, LOSE, DRAW

    @Column(name = "point_change", nullable = false)
    private int pointChange;

    @Builder
    private QuizHistory(Quiz quiz, Member member, int score,
                        QuizResult result, int pointChange) {
        this.quiz = quiz;
        this.member = member;
        this.score = score;
        this.result = result;
        this.pointChange = pointChange;
    }

    public static QuizHistory create(Quiz quiz, Member member,
                                     int score, QuizResult result, int delta) {
        return QuizHistory.builder()
                .quiz(quiz)
                .member(member)
                .score(score)
                .result(result)
                .pointChange(delta)
                .build();
    }
}
