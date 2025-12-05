package org.hackathon.genon.domain.quizoption.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hackathon.genon.domain.question.entity.Question;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 각 문제에 속한 4개의 선택지와 그중 어떤 것이 정답인지를 기록합니다.
 * */

@Entity
@Table(name = "quiz_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)   // FK → quiz_question.question_id
    private Question question;

    @Column(name = "content", nullable = false)
    private String content;      // 보기 내용

    @Column(name = "is_correct", nullable = false)
    private boolean correct;     // 정답 여부

    @Builder
    private QuizOption(Question question, String content, boolean correct) {
        this.question = question;
        this.content = content;
        this.correct = correct;
    }

    public static QuizOption create(Question question, String content, boolean correct) {
        return QuizOption.builder()
                .question(question)
                .content(content)
                .correct(correct)
                .build();
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}