package org.hackathon.genon.domain.member.repository;

import java.util.Optional;
import org.hackathon.genon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByLoginId(String loginId);

    Optional<Member> findByLoginId(String loginId);
}