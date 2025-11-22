package org.hackathon.genon.domain.quizlog.entity;

import jakarta.persistence.Entity;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 한 게임에서 출제된 문제와 각 사용자의 선택을 기록합니다. 결과 화면에서 '풀었던 퀴즈 리스트'를 보여줄 때 사용됩니다.
 * */
@Entity
public class QuizLog extends BaseEntity {

}