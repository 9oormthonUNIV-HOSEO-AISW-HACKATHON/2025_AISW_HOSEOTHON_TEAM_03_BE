package org.hackathon.genon.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.hackathon.genon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByLoginId(String loginId);

    Optional<Member> findByLoginId(String loginId);

    @Query(value = "SELECT * FROM member ORDER BY points DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true
    )
    List<Member> findTopByPointsWithLimitOffset(@Param("limit") int limit, @Param("offset") int offset);

}