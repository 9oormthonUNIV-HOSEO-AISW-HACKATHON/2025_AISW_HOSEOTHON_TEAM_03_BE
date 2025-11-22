package org.hackathon.genon.global.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;
import org.hackathon.genon.global.error.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CoreException e) {
        ErrorStatus errorStatus = e.getErrorStatus();
        log.error("커스텀 예외: 상태코드 - {}, 메세지 - {}", errorStatus.getStatusCode(), errorStatus.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .statusCode(errorStatus.getStatusCode())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(errorStatus.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예외 발생: {}", e.getMessage());

        int statusCode = INTERNAL_SERVER_ERROR.value();
        ErrorResponse response = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        int statusCode = e.getStatusCode().value();
        ErrorResponse response = ErrorResponse.builder()
                .statusCode(statusCode)
                .message("유효하지 않은 파라미터입니다.")
                .build();

        e.getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            log.warn("유효성 검사 실패 - 필드: {}, 메시지: {}", field, errorMessage);
            response.addValidation(field, errorMessage);
        });

        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentMismatchException(MethodArgumentTypeMismatchException e) {
        int statusCode = BAD_REQUEST.value();
        String paramName = e.getParameter().getParameterName();
        String paramType = e.getParameter().getParameterType().getSimpleName();
        String detailMessage = e.getMessage();
        String message = "[" + paramName + "] 파라미터는 " + paramType + " 타입이어야 합니다. 상세: " + detailMessage;
        log.warn("파라미터 타입 불일치: {}", message);

        ErrorResponse response = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(message)
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        int statusCode = BAD_REQUEST.value();
        String paramName = e.getParameterName();
        String paramType = e.getParameterType();
        String message = paramType + " 타입의" + " [ " + paramName + " ] " + "파라미터가 누락되었습니다.";
        log.warn("파라미터 누락: {}", message);

        ErrorResponse response = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(message)
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleRequestBodyMissing(HttpMessageNotReadableException ex) {
        int statusCode = BAD_REQUEST.value();
        String message = "요청 바디가 올바르지 않거나 누락되었습니다: " + ex.getMessage();
        log.warn("RequestBodyException: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(message)
                .build();

        return ResponseEntity.status(statusCode).body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(HandlerMethodValidationException ex) {
        int statusCode = BAD_REQUEST.value();
        String message = "유효성 검증에 실패하셨습니다.: " + ex.getMessage();
        log.warn("HandlerMethodValidationException: {}", message);

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(statusCode)
                .message(message)
                .build();

        return ResponseEntity.status(statusCode).body(error);
    }

}