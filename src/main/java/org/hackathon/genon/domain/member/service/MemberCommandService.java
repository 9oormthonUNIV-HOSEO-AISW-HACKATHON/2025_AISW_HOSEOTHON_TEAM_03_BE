package org.hackathon.genon.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.domain.member.service.dto.MemberCreateProfile;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.error.ErrorStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long register(MemberCreateProfile memberCreateProfile) {
        validateNewMember(memberCreateProfile);

        return memberRepository.save(Member.create(
                        memberCreateProfile.loginId(),
                        passwordEncoder.encode(memberCreateProfile.password()),
                        memberCreateProfile.nickname()
                )
        ).getId();

    }


    private void validateNewMember(MemberCreateProfile memberCreateProfile) {
        if (memberRepository.existsByNickname(memberCreateProfile.nickname())) {
            throw new CoreException(ErrorStatus.DUPLICATE_NICKNAME);
        }
        if (memberRepository.existsByLoginId(memberCreateProfile.loginId())) {
            throw new CoreException(ErrorStatus.DUPLICATE_LOGIN_ID);
        }
    }

}