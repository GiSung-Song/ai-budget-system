package com.budget.ai.response;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 인증
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_LOGIN_REQUEST("INVALID_LOGIN_REQUEST", "로그인 요청이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    MISSING_JWT_PAYLOAD("MISSING_JWT_PAYLOAD", "JWT 페이로드가 누락되었습니다.", HttpStatus.BAD_REQUEST),

    // 사용자
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELETED_USER("DELETED_USER", "탈퇴된 사용자입니다.", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "이미 존재하는 사용자입니다.", HttpStatus.CONFLICT),
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    USER_ALREADY_DELETED("USER_ALREADY_DELETED", "이미 탈퇴한 사용자입니다.", HttpStatus.CONFLICT),

    // 카드
    CARD_ALREADY_EXISTS("CARD_ALREADY_EXISTS", "이미 등록된 카드입니다.", HttpStatus.CONFLICT),
    CARD_NOT_FOUND("CARD_NOT_FOUND", "등록되지 않은 카드입니다.", HttpStatus.NOT_FOUND),

    // 카드 거래내역
    CARD_TRANSACTION_ALREADY_EXISTS("CARD_TRANSACTION_ALREADY_EXISTS", "이미 존재하는 거래번호입니다.", HttpStatus.CONFLICT),

    // API 호출
    API_CALL_CLIENT_ERROR("API_CALL_CLIENT_ERROR", "API 호출 중 클라이언트 오류가 발생하였습니다.", HttpStatus.BAD_GATEWAY),
    API_CALL_SERVER_ERROR("API_CALL_SERVER_ERROR", "API 호출 중 서버 오류가 발생하였습니다.", HttpStatus.BAD_GATEWAY),
    API_CALL_TIMEOUT("API_CALL_TIMEOUT", "API 호출 중 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    API_CALL_WRONG_ANSWER("API_CALL_WRONG_ANSWER", "API 호출 중 올바르지 않은 응답입니다.", HttpStatus.BAD_GATEWAY),
    API_RATE_LIMIT_EXCEEDED("API_RATE_LIMIT_EXCEEDED", "API 호출 중 오류가 발생하였습니다.", HttpStatus.BAD_GATEWAY),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}