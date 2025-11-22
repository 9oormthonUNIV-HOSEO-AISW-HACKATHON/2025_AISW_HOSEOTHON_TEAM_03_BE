package org.hackathon.genon.domain.quizhistory.repository;

import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {

}