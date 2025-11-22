package org.hackathon.genon.domain.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 문제의 순수한 '질문'과 '해설' 정보만 담습니다.
 * */

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Column(name = "category", length = 20, nullable = false)
    private String category;       // 'SENIOR', 'MZ'

    @Column(name = "content", length = 255, nullable = false)
    private String content;        // 문제 내용

    @Lob
    @Column(name = "explanation")
    private String explanation;    // 문제 해설

    @Builder
    private Question(String category, String content, String explanation) {
        this.category = category;
        this.content = content;
        this.explanation = explanation;
    }

    public static Question create(String category, String content, String explanation) {
        return Question.builder()
                .category(category)
                .content(content)
                .explanation(explanation)
                .build();
    }
}