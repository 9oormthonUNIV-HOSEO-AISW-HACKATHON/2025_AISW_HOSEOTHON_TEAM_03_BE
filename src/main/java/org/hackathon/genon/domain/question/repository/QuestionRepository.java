package org.hackathon.genon.domain.question.repository;

import org.hackathon.genon.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}