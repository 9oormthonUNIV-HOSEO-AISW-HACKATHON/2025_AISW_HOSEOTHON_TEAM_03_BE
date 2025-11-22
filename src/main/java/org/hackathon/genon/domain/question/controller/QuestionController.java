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

    @PostMapping("/generate")
    public ResponseEntity<List<QuestionResponseDto>> generateQuestionsByAi() {
        List<QuestionResponseDto> result = questionAiService.generateAndReturnQuestionsWithOptions();
        return ResponseEntity.ok(result);
    }
}
