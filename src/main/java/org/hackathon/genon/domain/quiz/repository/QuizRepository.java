package org.hackathon.genon.domain.quiz.repository;

import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

}