package org.hackathon.genon.domain.ranking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hackathon.genon.domain.ranking.controller.dto.RankingListResponse;
import org.hackathon.genon.global.error.ErrorStatus;
import org.hackathon.genon.global.swagger.ApiExceptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "랭킹 API", description = "랭킹 관련 API 문서입니다.")
public abstract class RankingDocsController {

    @Operation(
            summary = "랭킹 조회 - JWT O",
            description = "페이지네이션을 통해 랭킹 목록을 조회합니다."
                    + ""
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "랭킹 조회 성공",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RankingListResponse.class)
                            )
                    }
            )
    })
    @ApiExceptions(values = {
            ErrorStatus.BAD_REQUEST,
            ErrorStatus.UNAUTHORIZED_ERROR,
            ErrorStatus.FORBIDDEN_ERROR,
            ErrorStatus.INTERNAL_SERVER_ERROR
    })
    public abstract ResponseEntity<RankingListResponse> getRankings(
            @Parameter(description = "조회할 페이지 번호", example = "1", required = true)
            int page
    );

}