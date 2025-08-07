package com.budget.ai.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

/**
 * API 성공 응답 DTO
 * <p>
 * - HTTP 상태 코드, 메시지, 데이터 포함
 * - 정적 팩토리 메서드로 다양한 생성 방식 지원
 * </p>
 * @param <T> 응답 데이터 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class SuccessResponse<T> {

    /** HTTP 상태 코드 (예: 200, 201 등) */
    private final int status;

    /** 응답 메시지 (예: "성공", "회원가입 성공" 등) */
    private final String message;

    /** 응답 데이터 (없을 경우 null) */
    private final T data;

    /**
     * 상태코드와 데이터만 받는 생성자 (메시지는 기본값 "성공")
     * @param status HTTP 상태 코드
     * @param data   응답 데이터
     */
    public SuccessResponse(int status, T data) {
        this.status = status;
        this.message = "성공";
        this.data = data;
    }

    /**
     * 상태코드, 메시지, 데이터 모두 받는 생성자
     * @param status  HTTP 상태 코드
     * @param message 응답 메시지
     * @param data    응답 데이터
     */
    public SuccessResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * HTTP 상태 코드, 메시지, 데이터로 객체 생성
     * @param status  HTTP 상태 코드
     * @param message 응답 메시지
     * @param data    응답 데이터
     * @return SuccessResponse 객체
     */
    public static <T> SuccessResponse<T> of(HttpStatus status, String message, T data) {
        return new SuccessResponse<>(status.value(), message, data);
    }

    /**
     * HTTP 상태 코드, 데이터로 객체 생성 (메시지는 기본값 "성공")
     * @param status HTTP 상태 코드
     * @param data   응답 데이터
     * @return SuccessResponse 객체
     */
    public static <T> SuccessResponse<T> of(HttpStatus status, T data) {
        return new SuccessResponse<>(status.value(), data);
    }

    /**
     * HTTP 상태 코드로 객체 생성 (데이터 null, 메시지 "성공")
     * @param status HTTP 상태 코드
     * @return SuccessResponse 객체
     */
    public static <T> SuccessResponse<T> of(HttpStatus status) {
        return new SuccessResponse<>(status.value(), null);
    }

    /**
     * 데이터로 객체 생성 (상태 코드 200 OK, 메시지 "성공")
     * @param data 응답 데이터
     * @return SuccessResponse 객체
     */
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(HttpStatus.OK.value(), data);
    }

    /**
     * 기본 성공 객체 생성 (상태 코드 200 OK, 데이터 null, 메시지 "성공")
     * @return SuccessResponse 객체
     */
    public static <T> SuccessResponse<T> of() {
        return new SuccessResponse<>(HttpStatus.OK.value(), null);
    }
}