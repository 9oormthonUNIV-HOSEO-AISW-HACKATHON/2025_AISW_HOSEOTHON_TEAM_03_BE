package org.hackathon.genon.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.match.dto.MatchAcceptRequest;
import org.hackathon.genon.domain.match.dto.MatchResult;
import org.hackathon.genon.domain.match.service.GameService;
import org.hackathon.genon.domain.match.service.MatchService;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.global.security.jwt.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final GameService gameService;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    /**
     * 매칭 참여
     * - matched=false, roomId=null  → 큐에 들어가서 대기 중
     * - matched=true,  roomId!=null → 방 생성됨, 양쪽 수락 대기
     */
    @PostMapping("/join")
    public ResponseEntity<MatchResult> join(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        Long memberId = jwtProvider.getMemberIdFromToken(token);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. id=" + memberId));

        GenerationRole generationRole = member.getGenerationRole();

        MatchResult result = matchService.joinMatch(memberId, generationRole);

        if (result.getRoomId() != null) {
            gameService.onMatchCreated(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 매칭 수락/거절
     */
    @PostMapping("/{roomId}/accept")
    public ResponseEntity<String> acceptMatch(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MatchAcceptRequest request
    ) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        Long memberId = jwtProvider.getMemberIdFromToken(token);

        gameService.handleAccept(roomId, memberId, request.isAccept());

        return ResponseEntity.ok("OK");
    }
}
