package com.budget.ai.user;

import com.budget.ai.testsupport.RepositoryTest;
import com.budget.ai.testsupport.container.TestContainerManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Nested
    class 이메일로_회원_조회_테스트 {

        @Test
        void 회원_있는_경우_회원_반환() {
            User user = User.builder()
                    .email("test@email.com")
                    .name("테스터")
                    .password("password1234")
                    .build();

            userRepository.save(user);

            Optional<User> findUser = userRepository.findByEmail(user.getEmail());

            assertThat(findUser).isPresent();
            assertThat(findUser.get().getEmail()).isEqualTo(user.getEmail());
            assertThat(findUser.get().getPassword()).isEqualTo(user.getPassword());
            assertThat(findUser.get().getName()).isEqualTo(user.getName());
        }

        @Test
        void 회원_없는_경우_null_반환() {
            Optional<User> findUser = userRepository.findByEmail("test@email.com");

            assertThat(findUser).isEmpty();
        }
    }

    @Nested
    class 이메일_중복_확인 {

        @Test
        void 이메일_중복_시_true_반환() {
            User user = User.builder()
                    .email("test@email.com")
                    .name("테스터")
                    .password("password1234")
                    .build();

            userRepository.save(user);

            boolean existsByEmail = userRepository.existsByEmail(user.getEmail());

            assertThat(existsByEmail).isTrue();
        }

        @Test
        void 이메일_중복_아닐_시_false_반환() {
            boolean existsByEmail = userRepository.existsByEmail("test@email.com");

            assertThat(existsByEmail).isFalse();
        }
    }

    @Nested
    class 삭제된_회원_조회 {

        @Test
        void 이름과_이메일로_삭제된_회원_존재할_시_회원_반환() {
            User user = User.builder()
                    .email("test@email.com")
                    .name("테스터")
                    .password("password1234")
                    .build();

            User savedUser = userRepository.save(user);

            savedUser.softDelete();

            Optional<User> findUser = userRepository.findByNameAndEmailAndDeletedAtIsNotNull(user.getName(), user.getEmail());

            assertThat(findUser).isPresent();
            assertThat(findUser.get().getEmail()).isEqualTo(user.getEmail());
            assertThat(findUser.get().getName()).isEqualTo(user.getName());
        }

        @Test
        void 이름과_이메일로_삭제된_회원_존재하지_않을_시_null_반환() {
            User user = User.builder()
                    .email("test@email.com")
                    .name("테스터")
                    .password("password1234")
                    .build();

            userRepository.save(user);

            Optional<User> findUser = userRepository.findByNameAndEmailAndDeletedAtIsNotNull(user.getName(), user.getEmail());

            assertThat(findUser).isEmpty();
        }
    }
}