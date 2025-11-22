package org.hackathon.genon.domain.member.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.member.enums.Grade;

@Schema(description = "회원 프로필 응답 DTO")
@Builder
public record MemberProfileResponse(
        @Schema(description = "닉네임", example = "UserNickname")
        String nickname,

        @Schema(description = "세대 역할", example = "MZ")
        GenerationRole generationRole,

        @Schema(description = "포인트", example = "1500")
        Long points,

        @Schema(description = "회원 등급", example = "BRONZE")
        Grade grade
) {

    public static MemberProfileResponse from(Member member) {
        return MemberProfileResponse.builder()
                .nickname(member.getNickname())
                .generationRole(member.getGenerationRole())
                .points(member.getPoints())
                .grade(member.getGrade())
                .build();
    }

}