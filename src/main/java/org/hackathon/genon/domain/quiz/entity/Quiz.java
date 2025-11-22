package org.hackathon.genon.domain.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 퀴즈 자체에 대한 메타데이터만 기록합니다.
 */
@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

    @Column(name = "quiz_type", length = 20, nullable = false)
    private String quizType;  // ex) "1v1"

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_status", length = 20, nullable = false)
    private QuizStatus quizStatus;  // WAITING / IN_PROGRESS / FINISHED

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    private Quiz(String quizType, QuizStatus quizStatus) {
        this.quizType = quizType;
        this.quizStatus = quizStatus;
    }

    public static Quiz create(String quizType, QuizStatus quizStatus) {
        return Quiz.builder()
                .quizType(quizType)
                .quizStatus(quizStatus)
                .build();
    }

    public void start() {
        if (this.quizStatus == QuizStatus.FINISHED) {
            throw new IllegalStateException("종료된 게임은 시작할 수 없습니다.");
        }
        this.quizStatus = QuizStatus.IN_PROGRESS;
    }

    public void finish() {
        if (this.quizStatus != QuizStatus.FINISHED) {
            this.quizStatus = QuizStatus.FINISHED;
            this.endedAt = LocalDateTime.now();
        }
    }
}
