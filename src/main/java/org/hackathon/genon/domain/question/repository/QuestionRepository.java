package org.hackathon.genon.domain.question.repository;

import java.util.List;
import org.hackathon.genon.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 데이터베이스 종류에 따라 랜덤 쿼리가 다를 수 있음 (아래는 MySQL 예시)
    @Query(value = "SELECT * FROM question ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("limit") int limit);

}