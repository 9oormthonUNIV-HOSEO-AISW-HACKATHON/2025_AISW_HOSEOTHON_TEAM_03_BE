package org.hackathon.genon.domain.ranking.controller;

import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.ranking.service.RankingService;
import org.hackathon.genon.domain.ranking.service.dto.MemberRankingResponse;
import org.hackathon.genon.domain.ranking.controller.dto.RankingListResponse;
import org.hackathon.genon.global.support.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RankingController extends RankingDocsController{

    private final RankingService rankingService;

    @Override
    @GetMapping("/v1/rankings")
    public ResponseEntity<RankingListResponse> getRankings(
            @RequestParam int page
    ) {
        List<MemberRankingResponse> totalRanking = rankingService.getTotalRanking(new PageRequest(page));

        return ResponseEntity.ok(new RankingListResponse(totalRanking));
    }

}