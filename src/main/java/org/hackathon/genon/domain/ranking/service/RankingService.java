package org.hackathon.genon.domain.ranking.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.domain.ranking.service.dto.MemberRankingResponse;
import org.hackathon.genon.global.support.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RankingService {

    private final MemberRepository memberRepository;

    public List<MemberRankingResponse> getTotalRanking(PageRequest request) {
        List<Member> members = memberRepository.findTopByPointsWithLimitOffset(request.limit(), request.offset());

        return members.stream()
                .map(MemberRankingResponse::from)
                .toList();
    }

}