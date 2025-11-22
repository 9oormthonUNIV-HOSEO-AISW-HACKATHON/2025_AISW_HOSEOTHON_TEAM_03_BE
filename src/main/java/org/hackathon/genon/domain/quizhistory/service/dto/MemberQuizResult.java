package org.hackathon.genon.domain.quizhistory.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hackathon.genon.domain.quizhistory.entity.QuizResult;

@Schema(description = "사용자 퀴즈 결과 정보")
public record MemberQuizResult(

        @Schema(description = "닉네임", example = "UserNickname")
        String nickname,

        @Schema(description = "점수", example = "3")
        int score,

        @Schema(description = "퀴즈 결과", example = "WIN")
        QuizResult result, // "WIN", "LOSE", "DRAW"

        @Schema(description = "점수 변화량", example = "50")
        int pointChange,

        @Schema(description = "총 점수", example = "1500")
        Long totalPoints,

        @Schema(description = "본인 여부", example = "true")
        boolean isMe

) {

}