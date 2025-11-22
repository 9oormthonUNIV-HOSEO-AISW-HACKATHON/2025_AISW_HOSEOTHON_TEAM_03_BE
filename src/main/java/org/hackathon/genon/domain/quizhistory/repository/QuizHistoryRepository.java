package org.hackathon.genon.domain.quizhistory.repository;

import java.util.List;
import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {

    List<QuizHistory> findByQuizId(Long quizId);

}