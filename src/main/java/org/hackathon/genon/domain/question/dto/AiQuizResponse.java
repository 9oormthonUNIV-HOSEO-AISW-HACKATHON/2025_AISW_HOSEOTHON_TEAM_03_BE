package org.hackathon.genon.domain.question.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class AiQuizResponse {

    private List<AiQuizQuestion> questions;

    @Getter
    @NoArgsConstructor
    public static class AiQuizQuestion {
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
