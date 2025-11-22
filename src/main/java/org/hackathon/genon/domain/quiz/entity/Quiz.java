package org.hackathon.genon.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hackathon.genon.global.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 퀴즈자체에 대한 메타데이터만 기록합니다.
 */
@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

    @Column(name = "game_type", length = 20, nullable = false)
    private String gameType;           // ex) "1v1"

    @Column(name = "status", length = 20, nullable = false)
    private String status;             // WAITING / IN_PROGRESS / FINISHED

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    private Quiz(String gameType, String status) {
        this.gameType = gameType;
        this.status = status;
    }

    public void start() {
        if ("FINISHED".equals(this.status)) {
            throw new IllegalStateException("종료된 게임은 시작할 수 없습니다.");
        }
        this.status = "IN_PROGRESS";
    }

    public void finish() {
        if (!"FINISHED".equals(this.status)) {
            this.status = "FINISHED";
            this.endedAt = LocalDateTime.now();
        }
    }
}
