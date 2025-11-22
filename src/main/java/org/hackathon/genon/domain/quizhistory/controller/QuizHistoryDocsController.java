package org.hackathon.genon.domain.quizhistory.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hackathon.genon.domain.quizhistory.controller.dto.QuizResultPairResponse;
import org.hackathon.genon.global.error.ErrorStatus;
import org.hackathon.genon.global.swagger.ApiExceptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Quiz History", description = "퀴즈 히스토리 관련 API 문서")
public abstract class QuizHistoryDocsController {

    @Operation(
            summary = "퀴즈 히스토리 조회 API JWT - O",
            description = "특정 퀴즈에 대한 모든 회원의 퀴즈 결과 히스토리를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "퀴즈 종료 후 유저 결과 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuizResultPairResponse.class)
                    )
            )
    })
    @ApiExceptions(
            values = {
                    ErrorStatus.BAD_REQUEST,
                    ErrorStatus.UNAUTHORIZED_ERROR,
                    ErrorStatus.NOT_FOUND,
                    ErrorStatus.INTERNAL_SERVER_ERROR
            }
    )
    public abstract ResponseEntity<QuizResultPairResponse> getQuizHistory(Long quizId);

}