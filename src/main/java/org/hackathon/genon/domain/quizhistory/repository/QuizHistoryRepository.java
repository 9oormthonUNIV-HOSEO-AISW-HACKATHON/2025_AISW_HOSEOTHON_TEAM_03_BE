package org.hackathon.genon.domain.quizhistory.repository;

import java.util.List;
import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {

    @Query("SELECT qh FROM QuizHistory qh JOIN FETCH qh.member WHERE qh.quiz.id = :quizId")
    List<QuizHistory> findAllByQuizIdWithMember(@Param("quizId") Long quizId);
    List<QuizHistory> findByQuizId(Long quizId);


}