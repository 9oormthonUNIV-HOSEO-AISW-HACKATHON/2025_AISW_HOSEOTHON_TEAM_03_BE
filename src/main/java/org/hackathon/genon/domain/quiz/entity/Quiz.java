package org.hackathon.genon.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hackathon.genon.global.entity.BaseEntity;
import java.time.LocalDateTime;

/**
 * 퀴즈 자체에 대한 메타데이터만 기록합니다.
 */
@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

    @Column(name = "game_type", length = 20, nullable = false)
    private String quizType;  // ex) "1v1"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private QuizStatus status;  // WAITING / IN_PROGRESS / FINISHED

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    private Quiz(String quizType, QuizStatus status) {
        this.quizType = quizType;
        this.status = status;
    }

    public static Quiz create(String quizType, QuizStatus status) {
        return Quiz.builder()
                .quizType(quizType)
                .status(status)
                .build();
    }

    public void start() {
        if (this.status == QuizStatus.FINISHED) {
            throw new IllegalStateException("종료된 게임은 시작할 수 없습니다.");
        }
        this.status = QuizStatus.IN_PROGRESS;
    }

    public void finish() {
        if (this.status != QuizStatus.FINISHED) {
            this.status = QuizStatus.FINISHED;
            this.endedAt = LocalDateTime.now();
        }
    }
}
