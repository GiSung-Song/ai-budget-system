package com.budget.ai.user;

import com.budget.ai.auth.CustomUserDetails;
import com.budget.ai.response.SuccessResponse;
import com.budget.ai.user.dto.request.CancelDeleteRequest;
import com.budget.ai.user.dto.request.PasswordUpdateRequest;
import com.budget.ai.user.dto.request.RegisterRequest;
import com.budget.ai.user.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 Controller
 * <p>
 *     회원가입, 내 정보 조회, 비밀번호 변경, 회원탈퇴 기능 포함
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     */
    @Operation(summary = "회원가입", description = "회원가입 요청을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of());
    }

    /**
     * 내 정보 조회
     */
    @Operation(summary = "내 정보 조회", description = "자신의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserInfoResponse>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse myInfo = userService.getMyInfo(userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of(myInfo));
    }

    /**
     * 비밀번호 변경
     * @param request 비밀번호 변경 요청 DTO
     */
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터/현재 비밀번호 오류"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @PatchMapping("/me/password")
    public ResponseEntity<SuccessResponse<Void>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordUpdateRequest request) {
        userService.updatePassword(userDetails.id(), request);

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 회원 탈퇴
     */
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            @ApiResponse(responseCode = "409", description = "이미 탈퇴 처리중인 회원"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @DeleteMapping("/me")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.id());

        return ResponseEntity.ok(SuccessResponse.of());
    }

    /**
     * 회원 탈퇴 취소
     * @param request 회원 탈퇴 취소 요청 DTO
     */
    @Operation(summary = "회원 탈퇴 취소", description = "회원 탈퇴 취소를 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 취소 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "탈퇴되어 있지 않은 회원"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
    })
    @DeleteMapping("/me/deletion")
    public ResponseEntity<SuccessResponse<Void>> cancelDeleteUser(
            @RequestBody @Valid CancelDeleteRequest request) {
        userService.cancelDeleteUser(request);

        return ResponseEntity.ok(SuccessResponse.of());
    }
}
