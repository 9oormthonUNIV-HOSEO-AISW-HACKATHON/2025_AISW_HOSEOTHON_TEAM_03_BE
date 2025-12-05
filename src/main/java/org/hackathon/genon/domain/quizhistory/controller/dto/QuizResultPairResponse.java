package org.hackathon.genon.domain.quizhistory.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.hackathon.genon.domain.quizhistory.service.dto.MemberQuizResult;

@Schema(description = "퀴즈 결과 페어 응답 DTO")
public record QuizResultPairResponse(
        MemberQuizResult player1,
        MemberQuizResult player2
) {

    public static QuizResultPairResponse from(List<MemberQuizResult> memberQuizResults) {
        return new QuizResultPairResponse(
                memberQuizResults.get(0),
                memberQuizResults.get(1)
        );
    }

}
