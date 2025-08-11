package com.budget.ai.user;

import com.budget.ai.response.CustomException;
import com.budget.ai.response.ErrorCode;
import com.budget.ai.testsupport.ServiceTest;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.dto.request.CancelDeleteRequest;
import com.budget.ai.user.dto.request.PasswordUpdateRequest;
import com.budget.ai.user.dto.request.RegisterRequest;
import com.budget.ai.user.dto.response.UserInfoResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ServiceTest
class UserServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Nested
    class 회원가입_테스트 {

        @Test
        void 회원가입_성공() {
            RegisterRequest request = new RegisterRequest(
                    "tester@email.com",
                    "rawPassword",
                    "테스터"
            );

            userService.register(request);

            User findUser = userRepository.findByEmail(request.email())
                    .orElseThrow();

            assertThat(findUser.getEmail()).isEqualTo(request.email());
        }

        @Test
        void 중복된_이메일_409반환() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            RegisterRequest request = new RegisterRequest("tester@email.com", "홍길동", "rawPassword");

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
                    });
        }
    }

    @Nested
    class 회원정보_조회_테스트 {

        @Test
        void 회원정보_조회_성공() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            UserInfoResponse myInfo = userService.getMyInfo(user.getId());

            assertThat(myInfo.email()).isEqualTo(user.getEmail());
            assertThat(myInfo.name()).isEqualTo(user.getName());
            assertThat(myInfo.createdAt()).isEqualTo(user.getCreatedAt());
        }

        @Test
        void 회원_없음_404반환() {
            assertThatThrownBy(() -> userService.getMyInfo(4321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }
    }

    @Nested
    class 비밀번호_변경_테스트 {

        @Test
        void 비밀번호_변경_성공() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword", "newPassword");

            userService.updatePassword(user.getId(), request);

            assertThat(passwordEncoder.matches(request.newPassword(), user.getPassword())).isTrue();
        }

        @Test
        void 회원_없음_404반환() {
            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword", "newPassword");

            assertThatThrownBy(() -> userService.updatePassword(4321L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }

        @Test
        void 현재비밀번호_틀림_400반환() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword111", "newPassword");

            assertThatThrownBy(() -> userService.updatePassword(user.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURRENT_PASSWORD);
                    });
        }

        @Test
        void 현재비밀번호_새로운비밀번호_같음() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword", "rawPassword");

            userService.updatePassword(user.getId(), request);

            assertThat(passwordEncoder.matches(request.newPassword(), user.getPassword())).isTrue();
        }
    }

    @Nested
    class 회원_탈퇴_테스트 {

        @Test
        void 회원탈퇴_성공() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            userService.deleteUser(user.getId());

            User findUser = userRepository.findById(user.getId())
                    .orElseThrow();

            assertThat(findUser.getDeletedAt()).isNotNull();
        }

        @Test
        void 회원_없음_404반환() {
            assertThatThrownBy(() -> userService.deleteUser(4321L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }

        @Test
        void 이미_탈퇴_요청_상태_409반환() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            user.softDelete();

            assertThatThrownBy(() -> userService.deleteUser(user.getId()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_DELETED);
                    });
        }
    }

    @Nested
    class 회원_탈퇴_취소_테스트 {

        @Test
        void 회원_탈퇴_취소_성공() {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            user.softDelete();

            CancelDeleteRequest request = new CancelDeleteRequest(user.getEmail(), user.getName());

            userService.cancelDeleteUser(request);

            User findUser = userRepository.findById(user.getId())
                    .orElseThrow();

            assertThat(findUser.getDeletedAt()).isNull();
            assertThat(findUser.isDeleted()).isFalse();
        }

        @Test
        void 탈퇴한_회원_없음_404반환() {
            CancelDeleteRequest request = new CancelDeleteRequest("tester@email.com", "테스터");

            assertThatThrownBy(() -> userService.cancelDeleteUser(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException exception = (CustomException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                    });
        }
    }
}