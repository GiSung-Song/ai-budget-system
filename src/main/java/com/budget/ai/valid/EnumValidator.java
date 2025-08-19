package com.budget.ai.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    /** 검증할 Enum의 value Set */
    private Set<String> valueSet;

    /**
     * Validator 초기화 메서드
     * - 해당 Enum의 Value들을 대문자로 변환하여 Set으로 저장
     */
    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        valueSet = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(value -> value.name().toUpperCase())
                .collect(Collectors.toSet());
    }

    /**
     * 실제 검증 수행 메서드
     * - null 값 허용하지 않음
     * - 대소문자 상관없이 대문자로 치환하여 검사
     *
     * @param value   검증할 문자열
     * @param context ConstraintValidatorContext
     * @return 유효하면 true, 아니면 false 반환
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && valueSet.contains(value.toUpperCase());
    }
}
