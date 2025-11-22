package org.hackathon.genon.domain.ranking.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.member.enums.Grade;

@Schema(description = "회원 랭킹 응답 DTO")
@Builder
public record MemberRankingResponse(
        @Schema(description = "회원 닉네임", example = "UserNickname")
        String nickname,

        @Schema(description = "총 획득 포인트", example = "1500")
        Long totalPoints,

        @Schema(description = "학년", example = "FIRST, SECOND, THIRD, FOURTH")
        Grade grade,

        @Schema(description = "세대 역할", example = "MZ, SENIOR")
        GenerationRole generationRole
) {

    public static MemberRankingResponse from(Member member) {
        return MemberRankingResponse.builder()
                .nickname(member.getNickname())
                .totalPoints(member.getPoints())
                .grade(member.getGrade())
                .generationRole(member.getGenerationRole())
                .build();
    }

}