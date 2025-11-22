package org.hackathon.genon.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hackathon.genon.domain.auth.controller.dto.LoginRequest;
import org.hackathon.genon.global.error.ErrorStatus;
import org.hackathon.genon.global.security.jwt.dto.TokenResponse;
import org.hackathon.genon.global.swagger.ApiExceptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth Docs", description = "Authentication API 문서")
public abstract class AuthDocsController {

    @Operation(
            summary = "로그인 - JWT O",
            description = """
                    ### 사용자가 로그인하여 JWT를 발급받습니다.
                    - 요청 본문에 로그인 자격 증명을 포함해야 합니다.
                    - 성공 시 200 OK 상태 코드와 함께 JWT를 응답합니다.
                    """
    )
    @RequestBody(
            description = "로그인 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginRequest.class)
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공 및 JWT 발급",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TokenResponse.class)

            ))
    @ApiExceptions(
            values = {
                    ErrorStatus.BAD_REQUEST,
                    ErrorStatus.NOT_FOUND,
                    ErrorStatus.INTERNAL_SERVER_ERROR
            }
    )
    public abstract ResponseEntity<TokenResponse> login(LoginRequest request);

}