package org.hackathon.genon.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus {

    // COMMON
    BAD_REQUEST      (400, "잘못된 요청입니다."),
    FORBIDDEN_MODIFY (403, "수정 권한이 없습니다."),
    FORBIDDEN_DELETE (403, "삭제 권한이 없습니다."),
    NOT_FOUND        (404, "찾을 수 없습니다."),
    DUPLICATE        (409, "중복 되었습니다."),

    // MEMBER
    BAD_REQUEST_MEMBER     (400, "잘못된 회원 요청"),
    NOT_FOUND_MEMBER       (404, "회원을 찾을 수 없습니다."),
    NOT_FOUND_EMAIL        (404, "존재하지 않는 Email 입니다."),
    DUPLICATE_NICKNAME     (409, "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL        (409, "이미 존재하는 이메일 입니다."),
    ALREADY_SETUP_PROFILE  (409, "이미 프로필이 설정되어 있습니다."),
    PROFILE_SETUP_REQUIRED (428, "프로필 설정이 필요합니다."),


    // JWT
    INVALID_TOKEN        (401, "유효하지 않은 JWT 토큰입니다."),
    INVALID_SOCIAL_TOKEN (401, "유효하지 않은 소셜 토큰입니다."),
    NOT_FOUND_TOKEN      (404, "토큰을 찾을 수 없습니다."),

    // AWS
    INVALID_FILE_EXTENSION (400, "지원하지 않는 파일 확장자입니다."),
    NOT_FOUND_FILE         (404, "파일이 존재하지 않습니다."),
    AWS_S3_ERROR           (500, "AWS S3 내부 에러"),
    FAILED_TO_UPLOAD_FILE  (500, "파일 업로드에 실패하였습니다."),

    // ETC
    BAD_REQUEST_ARGUMENT   (400, "유효하지 않은 인자입니다."),
    UNAUTHORIZED_ERROR     (401, "인증되지 않은 사용자입니다."),
    EMPTY_SECURITY_CONTEXT (401, "Security Context 에 인증 정보가 없습니다."),
    FORBIDDEN_ERROR        (403, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR  (500, "서버 내부 에러"),

    ;

    private final int statusCode;
    private final String message;

}