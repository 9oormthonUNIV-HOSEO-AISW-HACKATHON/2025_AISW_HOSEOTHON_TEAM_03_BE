package org.hackathon.genon.domain.question.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.hackathon.genon.domain.question.dto.AiQuizResponse.AiQuizQuestion;
import org.hackathon.genon.domain.question.dto.QuestionOption;
import org.hackathon.genon.domain.question.dto.QuestionResponse;
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
    public List<QuestionResponse> generateAndReturnQuestionsWithOptions(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new CoreException(ErrorStatus.QUIZ_NOT_FOUND));

        AiQuizResponse response = null;
        try {
            response = callOpenAiForQuestions();
        } catch (Exception e) {
            log.warn("OpenAI 호출 실패, 기존 DB 문제로 대체 처리", e);
        }

        if (response != null && response.getQuestions() != null && !response.getQuestions().isEmpty()) {
            return saveQuestionsToDatabaseAndBuildDto(response.getQuestions(), quiz);
        } else {
            // OpenAI 실패 시 DB에 저장된 문제를 로드하여 DTO 변환 후 반환
            return loadQuestionsFromDatabaseAsDto();
        }
    }

    // OpenAI 호출
    private AiQuizResponse callOpenAiForQuestions() throws JsonProcessingException {
            Map<String, Object> body = buildOpenAiRequestBody();
            HttpHeaders headers = buildOpenAiHeaders();

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_COMPLETIONS_URL, HttpMethod.POST, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readValue(content, AiQuizResponse.class);

    }

    private List<QuestionResponse> loadQuestionsFromDatabaseAsDto() {
        List<Question> randomQuestions = questionRepository.findRandomQuestions(5);
        List<QuestionResponse> result = new ArrayList<>();

        for (Question question : randomQuestions) {

            List<QuestionOption> optionDtos = question.getOptions().stream()
                    .map(opt -> new QuestionOption(opt.getContent(), opt.isCorrect()))
                    .toList();

            QuestionResponse dto = new QuestionResponse(
                    question.getId(),
                    question.getCategory(),
                    question.getContent(),
                    question.getExplanation(),
                    optionDtos
            );
            result.add(dto);
        }
        return result;
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
    private List<QuestionResponse> saveQuestionsToDatabaseAndBuildDto(
            List<AiQuizQuestion> aiQuestions, Quiz quiz
    ) {
        List<QuestionResponse> result = new ArrayList<>();
        int round = 1;
        for (AiQuizQuestion qdto : aiQuestions) {
            Question question = saveQuestion(qdto);
            saveQuizQuestion(quiz, question, round++);
            List<QuestionOption> optionDtos = saveOptionsAndBuildDtos(qdto.getOptions(), question);
            result.add(buildQuestionResponseDto(question, optionDtos));
        }
        return result;
    }

    private Question saveQuestion(AiQuizQuestion qdto) {
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

    private List<QuestionOption> saveOptionsAndBuildDtos(
            List<AiQuizOptionDto> optionDtos, Question question
    ) {
        List<QuestionOption> result = new ArrayList<>();
        for (AiQuizOptionDto odto : optionDtos) {
            QuizOption option = QuizOption.create(question, odto.getContent(), odto.isCorrect());
            quizOptionRepository.save(option);
            question.addOption(option);
            result.add(new QuestionOption(odto.getContent(), odto.isCorrect()));
        }
        return result;
    }

    private QuestionResponse buildQuestionResponseDto(Question question, List<QuestionOption> optionDtos) {
        return new QuestionResponse(
                question.getId(),
                question.getCategory(),
                question.getContent(),
                question.getExplanation(),
                optionDtos
        );
    }

}