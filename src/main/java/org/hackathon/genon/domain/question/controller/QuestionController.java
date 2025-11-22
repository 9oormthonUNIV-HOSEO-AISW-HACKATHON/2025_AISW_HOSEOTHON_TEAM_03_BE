package org.hackathon.genon.domain.question.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.question.service.QuestionQueryService;
import org.hackathon.genon.domain.question.service.QuizQuestionAnswer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionController extends QuestionDocsController {

    private final QuestionQueryService questionQueryService;

    @Override
    @GetMapping("/v1/quiz/{quizId}/questions/results")
    public ResponseEntity<List<QuizQuestionAnswer>> findCorrectQuestionsFromQuiz(@PathVariable Long quizId) {
        List<QuizQuestionAnswer> questions = questionQueryService.findQuestions(quizId);

        return ResponseEntity.ok(questions);
    }

}
