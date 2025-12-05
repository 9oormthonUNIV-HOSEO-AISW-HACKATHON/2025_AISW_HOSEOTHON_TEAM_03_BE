package org.hackathon.genon.domain.question.service;

import lombok.Builder;
import org.hackathon.genon.domain.question.entity.Question;
import org.hackathon.genon.domain.quizoption.entity.QuizOption;

@Builder
public record QuizQuestionAnswer(
        Long questionId,
        String content,          // 문제 내용
        String correctOption,    // 정답 선택지 내용
        String explanation       // 해설
) {

    public static QuizQuestionAnswer from(Question question) {
        QuizOption correct = question.getCorrectOption();

        return QuizQuestionAnswer.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .correctOption(correct != null ? correct.getContent() : null)
                .explanation(question.getExplanation())
                .build();
    }

}
