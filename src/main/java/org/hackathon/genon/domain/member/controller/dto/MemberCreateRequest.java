package org.hackathon.genon.domain.member.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hackathon.genon.domain.member.service.dto.MemberCreateProfile;

@Schema(description = "회원 가입 요청 DTO")
public record MemberCreateRequest(
        @Schema(description = "로그인 아이디", example = "user123")
        @NotBlank(message = "로그인 아이디는 비어 있을 수 없습니다.")
        String loginId,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,

        @Schema(description = "닉네임", example = "UserNickname")
        @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
        String nickname
) {

    public MemberCreateProfile toCreateProfile() {
        return MemberCreateProfile.builder()
                .loginId(loginId)
                .password(password)
                .nickname(nickname)
                .build();
    }

}