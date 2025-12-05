package org.hackathon.genon.domain.question.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hackathon.genon.domain.quizoption.entity.QuizOption;
import org.hackathon.genon.global.entity.BaseEntity;

/**
 * 문제의 순수한 '질문'과 '해설' 정보만 담습니다.
 */
@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Column(name = "category", length = 20, nullable = false)
    private String category; // 'SENIOR', 'MZ'

    @Column(name = "content", length = 255, nullable = false)
    private String content; // 문제 내용

    @Lob
    @Column(name = "explanation")
    private String explanation; // 문제 해설

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizOption> options = new ArrayList<>();

    @Builder
    private Question(String category, String content, String explanation, List<QuizOption> options) {
        this.category = category;
        this.content = content;
        this.explanation = explanation;
        if (options != null) {
            this.options = options;
            // 양방향 연관관계 보장
            options.forEach(opt -> opt.setQuestion(this));
        }
    }

    public static Question create(String category, String content, String explanation) {
        return Question.builder()
                .category(category)
                .content(content)
                .explanation(explanation)
                .options(new ArrayList<>())
                .build();
    }

    public void addOption(QuizOption option) {
        options.add(option);
        option.setQuestion(this); // 편의 메서드로 양방향 연결
    }

    public QuizOption getCorrectOption() {
        return options.stream()
                .filter(QuizOption::isCorrect)
                .findFirst()
                .orElse(null);
    }

    public List<QuizOption> getCorrectOptions() {
        return options.stream()
                .filter(QuizOption::isCorrect)
                .toList();
    }
}
