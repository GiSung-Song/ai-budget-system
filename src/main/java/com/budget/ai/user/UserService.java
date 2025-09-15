package com.budget.ai.user;

import com.budget.ai.logging.AuditLogUtil;
import com.budget.ai.logging.aop.AuditLEntityId;
import com.budget.ai.logging.aop.AuditLog;
import com.budget.ai.logging.aop.OperationLog;
import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.user.dto.request.CancelDeleteRequest;
import com.budget.ai.user.dto.request.PasswordUpdateRequest;
import com.budget.ai.user.dto.response.UserInfoResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.budget.ai.user.dto.request.RegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관련 Service
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO (이메일, 이름, 비밀번호)
     */
    @Transactional
    @OperationLog(eventName = "회원가입")
    public void register(RegisterRequest request) {
        User savedUser = null;
        boolean success = false;
        String message = null;

        try {
            if (userRepository.existsByEmail(request.email())) {
                throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
            }

            User user = User.builder()
                    .email(request.email())
                    .name(request.name())
                    .password(passwordEncoder.encode(request.password()))
                    .build();

            savedUser = userRepository.save(user);

            success = true;
            message = "회원가입 완료";
        } catch (CustomException exception) {
            message = "ErrorCode: " + exception.getErrorCode().getCode() + ", Message: " + exception.getErrorCode().getMessage();
            throw exception;
        } finally {
            AuditLogUtil.logAudit(
                    this,
                    new Object[]{request},
                    "회원가입",
                    "register",
                    "INSERT",
                    "users",
                    savedUser != null ? String.valueOf(savedUser.getId()) : null,
                    message,
                    success
            );
        }
    }

    /**
     * 회원 정보 조회
     * @param userId 회원 식별자 ID
     * @return UserInfoResponse 회원정보 응답 DTO (이름, 이메일, 생성일자)
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(user);
    }

    /**
     * 비밀번호 변경
     * @param userId  회원 식별자 ID
     * @param request 비밀번호 변경 요청 DTO (현재 비밀번호, 새로운 비밀번호)
     */
    @Transactional
    @AuditLog(eventName = "비밀번호 변경", operationType = "UPDATE", entity = "users")
    @OperationLog(eventName = "비밀번호 변경")
    public void updatePassword(@AuditLEntityId Long userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            return;
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 회원 탈퇴 (soft delete)
     * @param userId 회원 식별자 ID
     */
    @Transactional
    @AuditLog(eventName = "회원 탈퇴", operationType = "UPDATE", entity = "users")
    @OperationLog(eventName = "회원 탈퇴")
    public void deleteUser(@AuditLEntityId Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }

        user.softDelete();
    }

    /**
     * 회원 탈퇴 취소 (soft delete cancel)
     * @param request 회원 탈퇴 취소 요청 DTO (이름, 이메일)
     */
    @Transactional
    @OperationLog(eventName = "회원 탈퇴 취소")
    public void cancelDeleteUser(CancelDeleteRequest request) {
        User user = null;
        boolean success = false;
        String message = null;

        try {
            user = userRepository.findByNameAndEmailAndDeletedAtIsNotNull(request.name(), request.email())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            user.cancelSoftDelete();

            success = true;
            message = "회원 탈퇴 취소 완료";
        } catch (CustomException exception) {
            message = "ErrorCode: " + exception.getErrorCode().getCode() + ", Message: " + exception.getErrorCode().getMessage();

            throw exception;
        } finally {
            AuditLogUtil.logAudit(
                    this,
                    new Object[]{request},
                    "회원 탈퇴 취소",
                    "cancelDeleteUser",
                    "UPDATE",
                    "users",
                    user != null ? String.valueOf(user.getId()) : null,
                    message,
                    success
            );
        }
    }
}
