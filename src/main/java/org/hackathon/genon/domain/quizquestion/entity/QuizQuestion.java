package org.hackathon.genon.domain.quizquestion.entity;

import jakarta.persistence.Entity;
import org.hackathon.genon.domain.question.entity.Question;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 어떤 게임에서 어떤 문제들이 출제되었는지를 기록하는 매핑 테이블입니다
 * */

@Entity
@Table(name = "quiz_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Quiz quiz;                   // 어떤 게임인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;           // 어떤 문제가 출제되었는지

    @Column(name = "round_number", nullable = false)
    private int roundNumber;             // 라운드 순서 (1~5)

    @Builder
    private QuizQuestion(Quiz quiz, Question question, int roundNumber) {
        this.quiz = quiz;
        this.question = question;
        this.roundNumber = roundNumber;
    }

    public static QuizQuestion create(Quiz quiz, Question question, int roundNumber) {
        return QuizQuestion.builder()
                .quiz(quiz)
                .question(question)
                .roundNumber(roundNumber)
                .build();
    }
}
