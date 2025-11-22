package org.hackathon.genon.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.domain.member.service.dto.MemberProfileResponse;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.error.ErrorStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorStatus.NOT_FOUND_MEMBER));

        return MemberProfileResponse.from(member);
    }

}