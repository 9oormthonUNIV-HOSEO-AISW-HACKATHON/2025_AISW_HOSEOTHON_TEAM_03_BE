package org.hackathon.genon.domain.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.member.repository.MemberRepository;
import org.hackathon.genon.domain.ranking.service.RankingService;
import org.hackathon.genon.global.support.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RankingServiceTest {

    @Autowired
    RankingService rankingService;

    @Autowired
    MemberRepository memberRepository;

    private final Random random = new Random();

    @DisplayName("랭킹을 조회한다.")
    @Test
    void getRanking() {
        // given
        for (int i = 0; i < 10; i++) {
            createAndSaveRandomMember(i);
        }

        // when
        var rankings = rankingService.getTotalRanking(new PageRequest(1));

        // then: 내림차순 정렬인지 검사
        assertThat(rankings).isNotEmpty();

        long previousPoints = Long.MAX_VALUE;
        for (var member : rankings) {
            long currentPoints = member.totalPoints();
            assertThat(currentPoints).isLessThanOrEqualTo(previousPoints);
        }

    }

    private void createAndSaveRandomMember(int idx) {
        String nickname = generateRandomNickname(idx);
        int points = random.nextInt(1000); // 0 ~ 999 랜덤 포인트

        var member = Member.create(
                "loginId_" + nickname,
                "password123!",
                nickname,
                GenerationRole.MZ // 또는 랜덤 역할 할당 가능
        );
        member.increasePoints((long) points);
        memberRepository.save(member);

        System.out.println("생성된 멤버: " + nickname + ", 포인트: " + points);
    }

    private String generateRandomNickname(int idx) {
        String[] animals = {"Tiger", "Eagle", "Shark", "Wolf", "Lion", "Falcon", "Panther", "Dragon", "Hawk", "Bear"};
        return animals[idx % animals.length] + idx;
    }

}