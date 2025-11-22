package org.hackathon.genon.domain.member.service.dto;

import lombok.Builder;
import org.hackathon.genon.domain.member.enums.GenerationRole;

@Builder
public record MemberCreateProfile(
        String loginId,
        String password,
        String nickname,
        GenerationRole generationRole
) {

}