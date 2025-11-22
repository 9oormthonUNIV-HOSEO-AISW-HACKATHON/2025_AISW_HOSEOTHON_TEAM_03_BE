package org.hackathon.genon.domain.question.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.domain.question.dto.AiQuizResponse;
import org.hackathon.genon.domain.question.dto.AiQuizResponse.AiQuizOptionDto;
import org.hackathon.genon.domain.question.dto.AiQuizResponse.AiQuizQuestionDto;
import org.hackathon.genon.domain.question.dto.QuestionOptionDto;
import org.hackathon.genon.domain.question.dto.QuestionResponseDto;
import org.hackathon.genon.domain.question.entity.Question;
import org.hackathon.genon.domain.question.enums.QuestionsPrompt;
import org.hackathon.genon.domain.question.repository.QuestionRepository;
import org.hackathon.genon.domain.quiz.entity.Quiz;
import org.hackathon.genon.domain.quiz.repository.QuizOptionRepository;
import org.hackathon.genon.domain.quiz.repository.QuizRepository;
import org.hackathon.genon.domain.quizoption.entity.QuizOption;
import org.hackathon.genon.domain.quizquestion.entity.QuizQuestion;
import org.hackathon.genon.domain.quizquestion.repository.QuizQuestionRepository;
import org.hackathon.genon.global.error.CoreException;
import org.hackathon.genon.global.error.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionAiService {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    private final QuestionRepository questionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;
    @Value("${openai.model}")
    private String model;
    @Value("${openai.temperature}")
    private Double temperature;
    @Value("${openai.max-output-tokens}")
    private Integer maxTokens;

    @Transactional
    public List<QuestionResponseDto> generateAndReturnQuestionsWithOptions(Long quizId) {
        AiQuizResponse response = callOpenAiForQuestions();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new CoreException(ErrorStatus.QUIZ_NOT_FOUND));
        return saveQuestionsToDatabaseAndBuildDto(response.getQuestions(), quiz);
    }

    // OpenAI 호출
    private AiQuizResponse callOpenAiForQuestions() {
        try {
            Map<String, Object> body = buildOpenAiRequestBody();
            HttpHeaders headers = buildOpenAiHeaders();

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_COMPLETIONS_URL, HttpMethod.POST, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readValue(content, AiQuizResponse.class);
        } catch (Exception e) {
            log.error("OpenAI 퀴즈 생성 실패", e);
            throw new CoreException("OpenAI API 호출 중 오류 발생");
        }
    }

    private Map<String, Object> buildOpenAiRequestBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", QuestionsPrompt.SYSTEM_PROMPT.getDescription()),
                Map.of("role", "user", "content", QuestionsPrompt.USER_PROMPT.getDescription())
        );
        body.put("messages", messages);
        return body;
    }

    private HttpHeaders buildOpenAiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    // 전체 생성/저장/DTO 빌드 (각 단계 메서드 분리)
    private List<QuestionResponseDto> saveQuestionsToDatabaseAndBuildDto(
            List<AiQuizQuestionDto> aiQuestions, Quiz quiz
    ) {
        List<QuestionResponseDto> result = new ArrayList<>();
        int round = 1;
        for (AiQuizQuestionDto qdto : aiQuestions) {
            Question question = saveQuestion(qdto);
            saveQuizQuestion(quiz, question, round++);
            List<QuestionOptionDto> optionDtos = saveOptionsAndBuildDtos(qdto.getOptions(), question);
            result.add(buildQuestionResponseDto(question, optionDtos));
        }
        return result;
    }

    private Question saveQuestion(AiQuizQuestionDto qdto) {
        Question question = Question.create(
                qdto.getCategory(),
                qdto.getContent(),
                qdto.getExplanation()
        );
        return questionRepository.save(question);
    }

    private void saveQuizQuestion(Quiz quiz, Question question, int roundNumber) {
        QuizQuestion quizQuestion = QuizQuestion.create(quiz, question, roundNumber);
        quizQuestionRepository.save(quizQuestion);
    }

    private List<QuestionOptionDto> saveOptionsAndBuildDtos(
            List<AiQuizOptionDto> optionDtos, Question question
    ) {
        List<QuestionOptionDto> result = new ArrayList<>();
        for (AiQuizOptionDto odto : optionDtos) {
            QuizOption option = QuizOption.create(question, odto.getContent(), odto.isCorrect());
            quizOptionRepository.save(option);
            question.addOption(option);
            result.add(new QuestionOptionDto(odto.getContent(), odto.isCorrect()));
        }
        return result;
    }

    private QuestionResponseDto buildQuestionResponseDto(Question question, List<QuestionOptionDto> optionDtos) {
        return new QuestionResponseDto(
                question.getId(),
                question.getCategory(),
                question.getContent(),
                question.getExplanation(),
                optionDtos
        );
    }

}