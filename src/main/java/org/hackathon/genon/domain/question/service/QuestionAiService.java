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
import org.hackathon.genon.domain.question.repository.QuestionRepository;
import org.hackathon.genon.domain.quiz.repository.QuizOptionRepository;
import org.hackathon.genon.domain.quizoption.entity.QuizOption;
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
            String systemPrompt = """
        당신은 MZ세대와 시니어 세대가 서로 이해하도록 돕는 퀴즈 출제자입니다.
        아래 조건을 반드시 지키세요.

        [문제 출제 조건]
        - 총 5문제만 생성한다.
        - 일부는 시니어 세대의 추억/문화, 일부는 MZ 세대의 트렌드/문화, 일부는 서로 공감할 만한 주제로 만든다.
        - 각 문제는 객관식 4지선다 형식이어야 한다.
        - 정답은 항상 1개만 존재해야 한다.
        - 난이도는 너무 어렵지 않게, 대화하듯이 자연스러운 질문으로 만든다.
        - 답이 명확하게 하나로 떨어지는 문제만 출제한다.

        [JSON 형식 규칙]
        - 출력은 반드시 JSON 객체 하나만 포함해야 한다. (설명 문장, 마크다운, 코드블록 금지)
        - 최상위 구조는 아래와 같다.
          {
            "questions": [ ... 5개의 문제 ... ]
          }
        - 각 questions[i] 는 아래 필드를 가진다.
          - "category": "MZ" 또는 "SENIOR" 무조건 이 둘중 하나
          - "content": 질문 내용 (문자열)
          - "explanation": 정답에 대한 짧은 해설 (문자열)
          - "options": 보기 4개가 들어 있는 배열 (길이 4)

        [options 규칙]
        - options 는 정확히 4개의 요소를 가진 배열이어야 한다.
        - 각 보기 객체는 다음과 같은 형식을 따른다.
          {
            "content": "보기 내용",
            "correct": true 또는 false
          }
        - 한 문제에서 "correct": true 인 보기 객체는 정확히 1개만 존재해야 한다.
        - content, explanation, options.content 안에서는 큰따옴표(")를 사용하지 않는다.

        [JSON 예시]
        {
          "questions": [
            {
              "category": "MZ",
              "content": "질문 내용",
              "explanation": "해설",
              "options": [
                { "content": "보기1", "correct": false },
                { "content": "보기2", "correct": true },
                { "content": "보기3", "correct": false },
                { "content": "보기4", "correct": false }
              ]
            }
          ]
        }
        """;


            String userPrompt = "위 조건에 맞게 서로 다른 퀴즈 5개를 JSON으로 생성해줘.";

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
            throw new IllegalStateException("OpenAI API 호출 중 오류 발생", e);
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
