package org.hackathon.genon.domain.quizhistory.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.quizhistory.controller.dto.QuizResultPairResponse;
import org.hackathon.genon.domain.quizhistory.service.QuizHistoryQueryService;
import org.hackathon.genon.domain.quizhistory.service.dto.MemberQuizResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class QuizHistoryController extends QuizHistoryDocsController{

    private final QuizHistoryQueryService quizHistoryQueryService;

    @Override
    @GetMapping("/v1/quiz-histories/{quizId}")
    public ResponseEntity<QuizResultPairResponse> getQuizHistory(@PathVariable Long quizId) {

        List<MemberQuizResult> quizResult = quizHistoryQueryService.findQuizResult(quizId);

        return ResponseEntity.ok(QuizResultPairResponse.from(quizResult));

    }

}