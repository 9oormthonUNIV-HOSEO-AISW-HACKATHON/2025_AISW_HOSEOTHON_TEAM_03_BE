package org.hackathon.genon.domain.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record LoginRequest(
        @Schema(description = "로그인 아이디", example = "user123")
        @NotBlank(message = "로그인 아이디는 비어 있을 수 없습니다.")
        String loginId,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
        String password
) {

}