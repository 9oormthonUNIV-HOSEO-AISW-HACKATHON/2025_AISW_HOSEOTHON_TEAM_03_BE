package org.hackathon.genon.domain.quiz.repository;

import org.hackathon.genon.domain.quiz.entity.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {

}