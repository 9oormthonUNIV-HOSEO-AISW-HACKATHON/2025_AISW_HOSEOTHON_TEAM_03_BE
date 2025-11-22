package org.hackathon.genon.domain.member.controller;

import static org.hackathon.genon.global.error.ErrorStatus.BAD_REQUEST;
import static org.hackathon.genon.global.error.ErrorStatus.INTERNAL_SERVER_ERROR;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hackathon.genon.domain.member.controller.dto.MemberCreateRequest;
import org.hackathon.genon.global.swagger.ApiExceptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Member Docs", description = "Member API 문서")
public abstract class MemberDocsController {

    @Operation(
            summary = "신규 회원 생성 - JWT X",
            description = """
                    ### 신규 회원을 생성합니다.
                    - 요청 본문에 회원 생성에 필요한 정보를 포함해야 합니다.
                    - 성공 시 200 Created 상태 코드와 함께 응답합니다.
                    """
    )
    @RequestBody(
            description = "신규 회원 생성 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MemberCreateRequest.class)
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "신규 회원 생성 성공"
    )

    @ApiExceptions(values = {
            BAD_REQUEST,
            INTERNAL_SERVER_ERROR
    })
    public abstract ResponseEntity<Void> create(MemberCreateRequest request);

}