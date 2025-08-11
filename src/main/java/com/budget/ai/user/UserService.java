package com.budget.ai.user;

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
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
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
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
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
    public void deleteUser(Long userId) {
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
    public void cancelDeleteUser(CancelDeleteRequest request) {
        User user = userRepository.findByNameAndEmailAndDeletedAtIsNotNull(request.name(), request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.cancelSoftDelete();
    }
}
