package org.hackathon.genon.domain.member.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hackathon.genon.domain.member.enums.Grade;
import org.hackathon.genon.domain.member.enums.Role;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 회원 정보를 담습니다.
 */

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Column(nullable = false)
    private Long points;

    private String refreshToken;

    public static Member create(
            String loginId,
            String password,
            String nickname
    ) {
        Member member = new Member();
        member.loginId = loginId;
        member.password = password;
        member.nickname = nickname;
        member.role = Role.ROLE_MEMBER;
        member.grade = Grade.BRONZE;
        member.points = 0L;
        member.refreshToken = null;
        return member;
    }

    public void increasePoints(Long points) {
        this.points += points;
        updateGrade(points);
    }

    public void decreasePoints(Long points) {
        if (this.points - points <= 0) {
            this.points = points;
        }

        this.points -= points;
        updateGrade(points);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;

    }

    private void updateGrade(Long points) {
        grade = Grade.isUpdate(points);
    }

}