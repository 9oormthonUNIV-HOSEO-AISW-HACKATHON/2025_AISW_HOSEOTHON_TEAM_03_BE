package org.hackathon.genon.domain.question.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
public class AiQuizResponse {

    private List<AiQuizQuestionDto> questions;

    @Getter
    @NoArgsConstructor
    public static class AiQuizQuestionDto {
        private String category;      // "MZ" or "SENIOR"
        private String content;       // 문제 내용
        private String explanation;   // 해설
        private List<AiQuizOptionDto> options;
    }

    @Getter
    @NoArgsConstructor
    public static class AiQuizOptionDto {
        private String content;   // 보기 내용
        private boolean correct;  // 정답 여부
    }
}
