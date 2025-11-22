package org.hackathon.genon.domain.question.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.question.dto.QuestionResponseDto;
import org.hackathon.genon.domain.question.service.QuestionAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionAiService questionAiService;

    // NOTE: TEST 용 API - 실제 운영 환경에서는 제거 필요
    @PostMapping("/generate")
    public ResponseEntity<List<QuestionResponseDto>> generateQuestionsByAi() {
        List<QuestionResponseDto> result = questionAiService.generateAndReturnQuestionsWithOptions();
        return ResponseEntity.ok(result);
    }
}
