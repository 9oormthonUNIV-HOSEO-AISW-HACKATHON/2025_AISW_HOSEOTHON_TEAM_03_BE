package org.hackathon.genon.domain.quizparticipant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 *  어떤 유저가 어떤 게임에 참여했는지를 연결하는 매핑 테이블입니다.
 * */

@Entity
@Table(name = "quiz_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Builder
    private QuizParticipant(Quiz quiz, Member member, String team) {
        this.quiz = quiz;
        this.member = member;
    }

    public static QuizParticipant create(Quiz quiz, Member mem, String team) {
        return QuizParticipant.builder()
                .quiz(quiz)
                .member(mem)
                .build();
    }
}
