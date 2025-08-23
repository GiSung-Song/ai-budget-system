package com.budget.ai.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enum 값 검증용 커스텀 애노테이션
 */
@Constraint(validatedBy = EnumValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    /** 기본 검증 실패 메시지 */
    String message() default "유효하지 않은 값입니다.";

    /** 검증 그룹 지정 */
    Class<?>[] groups() default {};

    /** 페이로드 지정 */
    Class<? extends Payload>[] payload() default {};

    /** 검증할 Enum 클래스 지정 */
    Class<? extends Enum<?>> enumClass();
}
