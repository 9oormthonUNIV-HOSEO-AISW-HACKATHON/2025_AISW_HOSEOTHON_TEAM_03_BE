package org.hackathon.genon.domain.member.service.dto;

import lombok.Builder;

@Builder
public record MemberCreateProfile(
        String loginId,
        String password,
        String nickname
) {

}