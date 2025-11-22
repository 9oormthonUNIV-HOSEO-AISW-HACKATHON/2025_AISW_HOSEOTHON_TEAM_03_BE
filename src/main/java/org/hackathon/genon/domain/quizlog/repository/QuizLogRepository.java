package org.hackathon.genon.domain.quizlog.repository;

import org.hackathon.genon.domain.quizlog.entity.QuizLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizLogRepository extends JpaRepository<QuizLog, Long> {

}