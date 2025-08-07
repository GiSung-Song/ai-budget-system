package com.budget.ai.response;

import lombok.Getter;

/**
 * 애플리케이션 전용 커스텀 예외 클래스
 * - ErrorCode 포함하여 예외 유형과 메시지를 관리
 */
@Getter
public class CustomException extends RuntimeException {

    /** 예외에 대응하는 커스텀 에러 코드 */
    private final ErrorCode errorCode;

    /**
     * 에러 코드 기반 예외 생성자
     * @param errorCode 발생한 예외 코드
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}