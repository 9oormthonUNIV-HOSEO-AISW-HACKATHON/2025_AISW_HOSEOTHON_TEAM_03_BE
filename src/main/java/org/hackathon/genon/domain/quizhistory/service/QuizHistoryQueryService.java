package org.hackathon.genon.domain.quizhistory.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.hackathon.genon.domain.quizhistory.repository.QuizHistoryRepository;
import org.hackathon.genon.domain.quizhistory.service.dto.MemberQuizResult;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.error.ErrorStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QuizHistoryQueryService {

    private final QuizHistoryRepository quizHistoryRepository;

    public List<MemberQuizResult> findQuizResult(Long quizId, Long memberId) {
        List<QuizHistory> histories = quizHistoryRepository.findAllByQuizIdWithMember(quizId);

        if (histories.isEmpty()) {
            throw new CoreException(ErrorStatus.QUIZ_HISTORY_NOT_FOUND);
        }

        return histories.stream()
                .map(h -> new MemberQuizResult(
                        h.getMember().getNickname(),
                        h.getScore(),
                        h.getResult(),
                        h.getPointChange(),
                        h.getMember().getPoints(),
                        h.getMember().isMe(memberId)
                ))
                .toList();
    }

}