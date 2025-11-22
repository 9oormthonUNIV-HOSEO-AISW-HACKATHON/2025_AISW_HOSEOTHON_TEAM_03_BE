package org.hackathon.genon.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hackathon.genon.global.error.ErrorStatus;
import org.hackathon.genon.global.error.response.ErrorResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SwaggerConfig {

    private static final String AUTH_SCHEME = "Bearer "; // Swagger 보안 스키마명
    private static final String MEDIA_JSON = "application/json"; // 공통 미디어 타입

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(globalSecurityRequirement())
                .components(components()) // SecuritySchemes + ErrorResponse 스키마 등록
                .servers(List.of(
                        server("Development Server", "http://localhost:8080")
                ));
    } // 전역 OpenAPI 정의: 보안, 서버, 컴포넌트 등록

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/v1/admin/**")
                .addOperationCustomizer(operationCustomizer())
                .build();
    } // 관리자 API 그룹: 경로 기준 분리

    @Bean
    public GroupedOpenApi appApi() {
        return GroupedOpenApi.builder()
                .group("app")
                .pathsToMatch("/v1/**")
                .pathsToExclude("/v1/admin/**")
                .addOperationCustomizer(operationCustomizer())
                .build();
    } // 일반 앱 API 그룹: 공용 경로 구성

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 커스텀 예외 예시 주입
            ApiExceptions apiExceptions = handlerMethod.getMethodAnnotation(ApiExceptions.class);
            if (apiExceptions != null) {
                generateErrorCodeResponseExample(operation, apiExceptions.values());
            }
            return operation;
        };
    } // 오퍼레이션 커스터마이저: 공통 에러와 커스텀 에러 예시 적용

    private void generateErrorCodeResponseExample(Operation operation, ErrorStatus[] errorStatuses) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorStatuses)
                .map(errorStatus -> ExampleHolder.builder()
                        .holder(getSwaggerExample(errorStatus)) // Example
                        .name(errorStatus.name())               // String
                        .code(errorStatus.getStatusCode())      // int
                        .build()
                )
                .collect(Collectors.groupingBy(ExampleHolder::code));

        addExamplesToResponses(responses, statusWithExampleHolders);
    } // 제공한 ExampleHolder 레코드 빌더를 사용해 상태코드별로 그룹핑한다

    private Example getSwaggerExample(ErrorStatus errorStatus) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(errorStatus.getStatusCode())
                .message(errorStatus.getMessage())
                .build();
        Example example = new Example();
        example.setValue(errorResponse);
        return example;
    } // 기존 그대로 유지: ErrorStatus → Example 생성

    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, examples) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            examples.forEach(exampleHolder -> mediaType.addExamples(
                    exampleHolder.name(),
                    exampleHolder.holder()
            ));
            content.addMediaType(MEDIA_JSON, mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }  // 상태코드별 예시를 응답 콘텐츠로 추가

    private Info apiInfo() {
        return new Info()
                .title("GenOn API Docs")
                .description("GenOn 서비스 API 문서입니다.")
                .version("1.0.0");
    } // 문서 메타 정보 정의

    private SecurityRequirement globalSecurityRequirement() {
        return new SecurityRequirement().addList(AUTH_SCHEME);
    } // 전역 보안 요구사항: bearerAuth를 기본 적용

    private Components components() {
        return new Components()
                .addSecuritySchemes(AUTH_SCHEME, bearerSecurityScheme())
                .addSchemas("ErrorResponse", errorResponseSchema());
    } // 보안 스키마와 공통 에러 스키마 등록

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .name(AUTH_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("Authorization 헤더에 Bearer {accessToken} 형식으로 입력하세요.");
    } // JWT Bearer 스키마 정의(Authorize 버튼 사용)

    private Schema<?> errorResponseSchema() {
        return new Schema<>()
                .addProperty("statusCode", new IntegerSchema())
                .addProperty("message", new StringSchema());
    } // 공통 ErrorResponse 스키마(간단 예)

    private Server server(String description, String url) {
        return new Server().description(description).url(url);
    } // 서버 엔트리 생성 유틸

}
