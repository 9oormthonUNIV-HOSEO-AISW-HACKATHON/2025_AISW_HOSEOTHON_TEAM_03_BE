package org.hackathon.genon.domain.question.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.quizquestion.entity.QuizQuestion;
import org.hackathon.genon.domain.quizquestion.repository.QuizQuestionRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QuestionQueryService {

    private final QuizQuestionRepository quizQuestionRepository;

    public List<QuizQuestionAnswer> findQuestions(Long quizId) {
        // 가져올 떄 1라운드부터 가져옴.
        List<QuizQuestion> quizQuestions = quizQuestionRepository.findAllByQuizId(quizId);

        return quizQuestions.stream()
                .map(qq -> QuizQuestionAnswer.from(qq.getQuestion()))
                .toList();
    }

}