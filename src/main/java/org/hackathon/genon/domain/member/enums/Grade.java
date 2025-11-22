package org.hackathon.genon.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Grade {
    BRONZE(0L),
    SILVER(1000L),
    GOLD(5000L),
    PLATINUM(10000L),
    DIAMOND(20000L),
    MASTER(50000L),
    GRANDMASTER(100000L),
    CHALLENGER(200000L);

    private final Long requiredPoints;

    public static Grade isUpdate(Long points) {
        Grade result = null;
        for (Grade grade : Grade.values()) {
            if (points >= grade.getRequiredPoints()) {
                result = grade;
            } else {
                break; // 등급 기준이 오름차순으로 정렬되어 있다고 가정할 때 더 이상 탐색하지 않아도 됨
            }
        }
        return result;
    }


}