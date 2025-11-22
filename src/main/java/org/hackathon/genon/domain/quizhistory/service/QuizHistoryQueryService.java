package org.hackathon.genon.domain.quizhistory.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.hackathon.genon.domain.quizhistory.repository.QuizHistoryRepository;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.error.ErrorStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QuizHistoryQueryService {

    private final QuizHistoryRepository quizHistoryRepository;

    public void findQuizResult(Long quizId) {
        List<QuizHistory> histories = quizHistoryRepository.findByQuizId(quizId);
        if (histories.isEmpty()) {
            throw new CoreException(ErrorStatus.QUIZ_HISTORY_NOT_FOUND);
        }

    }
}
