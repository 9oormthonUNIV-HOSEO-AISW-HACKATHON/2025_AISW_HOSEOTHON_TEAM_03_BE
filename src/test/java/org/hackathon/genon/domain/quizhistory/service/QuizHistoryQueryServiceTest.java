package org.hackathon.genon.domain.quizhistory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.hackathon.genon.domain.member.entity.Member;
import org.hackathon.genon.domain.member.enums.GenerationRole;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.hackathon.genon.domain.quiz.entity.QuizStatus;
import org.hackathon.genon.domain.quizhistory.entity.QuizHistory;
import org.hackathon.genon.domain.quizhistory.entity.QuizResult;
import org.hackathon.genon.domain.quizhistory.repository.QuizHistoryRepository;
import org.hackathon.genon.domain.quizhistory.service.dto.MemberQuizResult;
import org.hackathon.genon.global.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizHistoryQueryServiceTest {

    @Mock
    private QuizHistoryRepository quizHistoryRepository;

    @InjectMocks
    private QuizHistoryQueryService quizHistoryQueryService;

    @DisplayName("퀴즈 결과가 존재하면 MemberQuizResult 리스트를 반환한다")
    @Test
    void findQuizResult_success() {
        // given
        var quizId = 1L;
        var mz = Member.create("loginId", "password", "mz", GenerationRole.MZ);
        var senior = Member.create("loginId2", "password", "senior", GenerationRole.SENIOR);
        var quiz = Quiz.create("1vs1", QuizStatus.FINISHED);
        QuizHistory history = QuizHistory.create(
                quiz,
                mz,
                2,
                QuizResult.LOSE,
                -10
        );
        QuizHistory history2 = QuizHistory.create(
                quiz,
                senior,
                3,
                QuizResult.WIN,
                10
        );

        given(quizHistoryRepository.findAllByQuizIdWithMember(quizId)).willReturn(List.of(history, history2));

        // when
        List<MemberQuizResult> result = quizHistoryQueryService.findQuizResult(quizId);

        // then
        assertThat(result).hasSize(2);
        MemberQuizResult res = result.getFirst();

        assertThat(res.nickname()).isEqualTo("mz");
        assertThat(res.score()).isEqualTo(2);
        assertThat(res.result()).isEqualTo(QuizResult.LOSE);
        assertThat(res.pointChange()).isEqualTo(-10);
        assertThat(res.totalPoints()).isEqualTo(0);
    }

    @DisplayName("퀴즈 결과가 없으면 CoreException이 발생한다")
    @Test
    void findQuizResult_fail() {
        // given
        Long quizId = 2L;
        given(quizHistoryRepository.findAllByQuizIdWithMember(quizId))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> quizHistoryQueryService.findQuizResult(quizId))
            .isInstanceOf(CoreException.class);
    }

}