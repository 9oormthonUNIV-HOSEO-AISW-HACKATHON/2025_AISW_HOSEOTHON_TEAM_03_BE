package org.hackathon.genon.domain.question.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.hackathon.genon.domain.question.service.QuizQuestionAnswer;
import org.hackathon.genon.global.error.ErrorStatus;
import org.hackathon.genon.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;

@Tag(name = "Question", description = "퀴즈 질문 관련 API 문서")
public abstract class QuestionDocsController {

    @Operation(
            summary = "퀴즈 종료 후 퀴즈 문제에 대한 정답과 해설 조회 요청 JWT O",
            description = """
                    ### 사용자가 푼 퀴즈의 문제들에 대한 정답과 해설을 조회합니다.
                    - 퀴즈가 종료된 후에만 접근할 수 있습니다.
                    - 요청 시 JWT 토큰이 필요합니다.
                    - 응답으로 퀴즈 문제에 대한 정답과 해설이 포함된 리스트를 반환합니다.
                    - 라운드 순으로 정렬되어 있습니다.
                    - 각 항목에는 문제 ID, 정답, 해설이 포함됩니다.
                    """
    )
    @ApiExceptions(
            values = {
                    ErrorStatus.BAD_REQUEST,
                    ErrorStatus.UNAUTHORIZED_ERROR,
                    ErrorStatus.FORBIDDEN_ERROR,
                    ErrorStatus.NOT_FOUND,
            }
    )
    public abstract ResponseEntity<List<QuizQuestionAnswer>> findCorrectQuestionsFromQuiz(Long quizId);
}
