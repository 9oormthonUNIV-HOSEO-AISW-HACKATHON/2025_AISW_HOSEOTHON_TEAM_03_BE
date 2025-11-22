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
import org.hackathon.genon.domain.quiz.repository.QuizOptionRepository;
import org.hackathon.genon.domain.quizoption.entity.QuizOption;
import org.hackathon.genon.global.error.CoreException;
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

    /**
     * OpenAI 호출 + Question 5개 생성 + DB 저장 + DTO 반환
     */
    @Transactional
    public List<QuestionResponseDto> generateAndReturnQuestionsWithOptions() {
        AiQuizResponse response = callOpenAiForQuestions();
        return saveQuestionsToDatabaseAndBuildDto(response);
    }

    /**
     * OpenAI API 호출 (Chat Completions)
     */
    private AiQuizResponse callOpenAiForQuestions() {
        try {
            String systemPrompt = QuestionsPrompt.SYSTEM_PROMPT.getDescription();
            String userPrompt = QuestionsPrompt.USER_PROMPT.getDescription();

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    CHAT_COMPLETIONS_URL, HttpMethod.POST, request, String.class);

            // OpenAI 응답 JSON에서 content 값만 추출
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            return objectMapper.readValue(content, AiQuizResponse.class);

        } catch (Exception e) {
            log.error("OpenAI 퀴즈 생성 실패", e);
            throw new CoreException("OpenAI API 호출 중 오류 발생");
        }
    }

    /**
     * Question + Options DB 저장 후 응답용 DTO 생성
     */
    private List<QuestionResponseDto> saveQuestionsToDatabaseAndBuildDto(AiQuizResponse response) {

        List<QuestionResponseDto> result = new ArrayList<>();

        for (AiQuizQuestionDto qdto : response.getQuestions()) {

            // 1) Question 저장
            Question question = Question.create(
                    qdto.getCategory(),
                    qdto.getContent(),
                    qdto.getExplanation()
            );
            questionRepository.save(question);

            // 2) 옵션 저장 + DTO 리스트 생성
            List<QuestionOptionDto> optionDtos = new ArrayList<>();

            for (AiQuizOptionDto odto : qdto.getOptions()) {
                QuizOption option = QuizOption.create(
                        question,
                        odto.getContent(),
                        odto.isCorrect()
                );
                quizOptionRepository.save(option);

                optionDtos.add(new QuestionOptionDto(
                        odto.getContent(),
                        odto.isCorrect()
                ));
            }

            // 3) 문제 + 보기 DTO 구성
            QuestionResponseDto dto = new QuestionResponseDto(
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
}
