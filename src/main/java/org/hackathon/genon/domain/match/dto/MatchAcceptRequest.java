package org.hackathon.genon.domain.match.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MatchAcceptRequest {

    /** true: 수락, false: 거절 */
    private boolean accept;
}
