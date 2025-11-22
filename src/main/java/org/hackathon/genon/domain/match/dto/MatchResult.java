package org.hackathon.genon.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {

    /** 매칭이 성사된 방 ID (둘 다 수락 대기용) */
    private String roomId;

    /** 내가 만난 상대 유저 ID */
    private Long opponentId;

    /** 매칭이 실제로 되었는지 여부 (true면 roomId != null) */
    private boolean matched;

    public static MatchResult waiting() {
        return MatchResult.builder()
                .matched(false)
                .build();
    }

    public static MatchResult matched(String roomId, Long opponentId) {
        return MatchResult.builder()
                .matched(true)
                .roomId(roomId)
                .opponentId(opponentId)
                .build();
    }
}
