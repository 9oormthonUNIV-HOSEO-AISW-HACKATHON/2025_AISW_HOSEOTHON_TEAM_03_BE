package org.hackathon.genon.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.domain.member.service.MemberCommandService;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.error.ErrorStatus;
import org.hackathon.genon.global.security.jwt.JwtProvider;
import org.hackathon.genon.global.security.jwt.dto.TokenResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse login(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CoreException(ErrorStatus.NOT_FOUND_MEMBER));

        validatePassword(password, member);

        TokenResponse tokens = jwtProvider.createTokens(member.getId(), member.getRole());

        member.updateRefreshToken(tokens.refreshToken());
        return tokens;
    }

    private void validatePassword(String password, Member member) {
        if (passwordEncoder.matches(password, member.getPassword())) {
            return;
        }
        throw new CoreException(ErrorStatus.BAD_REQUEST_MEMBER);
    }

}