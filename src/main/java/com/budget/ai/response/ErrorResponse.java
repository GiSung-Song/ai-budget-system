package com.budget.ai.response;

import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * API 에러 응답 DTO
 * <p>
 * - HTTP 상태 코드, 에러 코드, 에러 메시지, 필드 단위 검증 오류 목록 포함
 * - 정적 팩토리 메서드로 다양한 생성 방식 지원
 * </p>
 * @param status  HTTP 상태 코드 (예: 400, 404, 500 등)
 * @param code    에러 코드 (예: "USER_NOT_FOUND", "INVALID_PARAM" 등)
 * @param message 에러 메시지 (예: "필수값 누락", "권한 없음" 등)
 * @param errors  필드 단위 검증 오류 목록 (없으면 null)
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldError> errors
) {

    /**
     * 필드 단위 오류가 없는 에러 응답 생성
     * @param status  HTTP 상태 코드
     * @param code    에러 코드
     * @param message 에러 메시지
     * @return ErrorResponse 객체
     */
    public static ErrorResponse of(HttpStatus status, String code, String message) {
        return new ErrorResponse(status.value(), code, message, null);
    }

    /**
     * 필드 단위 오류가 있는 에러 응답 생성
     * @param status  HTTP 상태 코드
     * @param code    에러 코드
     * @param message 에러 메시지
     * @param errors  필드 오류 리스트
     * @return ErrorResponse 객체
     */
    public static ErrorResponse of(HttpStatus status, String code, String message, List<FieldError> errors) {
        return new ErrorResponse(status.value(), code, message, errors);
    }

    /**
     * 필드 단위 오류 정보 DTO
     * <p>
     * - 유효성 검증 실패 등에서 사용
     * </p>
     * @param field  오류가 발생한 필드명 (예: "email", "password")
     * @param reason 오류가 발생한 이유 (예: "필수값 누락", "형식 오류")
     */
    public record FieldError(String field, String reason) {}
}