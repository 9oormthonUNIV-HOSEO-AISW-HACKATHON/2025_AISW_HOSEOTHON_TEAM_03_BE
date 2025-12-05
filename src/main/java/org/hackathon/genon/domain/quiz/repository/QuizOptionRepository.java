package org.hackathon.genon.domain.quiz.repository;

import org.hackathon.genon.domain.quizoption.entity.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderByIdAsc(Long questionId);
}