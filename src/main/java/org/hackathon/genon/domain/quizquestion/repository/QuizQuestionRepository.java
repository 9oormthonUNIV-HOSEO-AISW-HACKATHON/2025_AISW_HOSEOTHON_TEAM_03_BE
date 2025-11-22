package org.hackathon.genon.domain.quizquestion.repository;

import java.util.List;
import org.hackathon.genon.domain.quizquestion.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    @Query("SELECT qq FROM QuizQuestion qq JOIN FETCH qq.question WHERE qq.quiz.id = :quizId ORDER BY qq.roundNumber ASC")
    List<QuizQuestion> findAllByQuizId(@Param("quizId") Long quizId);

}