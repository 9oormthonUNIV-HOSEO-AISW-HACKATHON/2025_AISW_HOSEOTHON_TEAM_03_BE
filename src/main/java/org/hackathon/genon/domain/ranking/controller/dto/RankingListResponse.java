package org.hackathon.genon.domain.ranking.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.hackathon.genon.domain.ranking.service.dto.MemberRankingResponse;

@Schema(description = "회원 랭킹 목록 응답 DTO")
public record RankingListResponse(
        List<MemberRankingResponse> responses
) {

}