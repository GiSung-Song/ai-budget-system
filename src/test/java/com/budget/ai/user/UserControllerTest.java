package com.budget.ai.user;

import com.budget.ai.auth.dto.response.TokenResponse;
import com.budget.ai.testsupport.ControllerTest;
import com.budget.ai.testsupport.TestAuthHelper;
import com.budget.ai.testsupport.TestDataFactory;
import com.budget.ai.testsupport.container.TestContainerManager;
import com.budget.ai.user.dto.request.CancelDeleteRequest;
import com.budget.ai.user.dto.request.PasswordUpdateRequest;
import com.budget.ai.user.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest
class UserControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @Nested
    class 회원가입_테스트 {

        @Test
        void 회원가입_성공() throws Exception {
            RegisterRequest request = new RegisterRequest("tester@email.com", "rawPassword", "테스터");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andDo(print());

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow();

            assertThat(user.getName()).isEqualTo(request.name());
            assertThat(user.getEmail()).isEqualTo(request.email());
        }

        @Test
        void 유효하지_않은_입력값_400반환() throws Exception {
            RegisterRequest request = new RegisterRequest(null, "rawPassword", "테스터");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 중복된_이메일_409반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            RegisterRequest request = new RegisterRequest("tester@email.com", "rawPassword", "테스터");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    class 내_정보_조회_테스트 {

        @Test
        void 내_정보_조회_성공() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            MvcResult result = mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.name").value("테스터"))
                    .andExpect(jsonPath("$.data.email").value("tester@email.com"))
                    .andReturn();

            String jsonString = result.getResponse().getContentAsString();
            String createdAtString = JsonPath.read(jsonString, "$.data.createdAt");

            assertThat(LocalDateTime.parse(createdAtString))
                    .isEqualTo(user.getCreatedAt());
        }

        @Test
        void 비_로그인_401반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        void 존재하지_않는_회원_404반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            userRepository.delete(user);

            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken()))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class 비밀번호_변경_테스트 {

        @Test
        void 비밀번호_변경_정상() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword", "newPassword");

            mockMvc.perform(patch("/api/users/me/password")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());

            User findUser = userRepository.findById(user.getId())
                    .orElseThrow();

            assertThat(passwordEncoder.matches("newPassword", findUser.getPassword())).isTrue();
        }

        @Test
        void 유효하지_않은_요청_데이터_400반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest(null, "newPassword");

            mockMvc.perform(patch("/api/users/me/password")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 일치하지_않은_현재_비밀번호_400반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest("rrawPassword", "newPassword");

            mockMvc.perform(patch("/api/users/me/password")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 비로그인_시_401반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword", "newPassword");

            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        void 존재하지_않은_회원_404반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            userRepository.delete(user);

            PasswordUpdateRequest request = new PasswordUpdateRequest("rawPassword", "newPassword");

            mockMvc.perform(patch("/api/users/me/password")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    class 회원_탈퇴_테스트 {

        @Test
        void 회원_탈퇴_성공() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            mockMvc.perform(delete("/api/users/me")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken()))
                    .andExpect(status().isOk())
                    .andDo(print());

            User findUser = userRepository.findById(user.getId())
                    .orElseThrow();

            assertThat(findUser.getDeletedAt()).isNotNull();
        }

        @Test
        void 비로그인_시_401반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            mockMvc.perform(delete("/api/users/me"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        void 존재하지_않는_회원_404반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            userRepository.delete(user);

            mockMvc.perform(delete("/api/users/me")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken()))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        void 이미_탈퇴처리_된_회원_409반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            TokenResponse tokenResponse = testAuthHelper.setLogin(user.getEmail(), "rawPassword");

            user.softDelete();

            mockMvc.perform(delete("/api/users/me")
                            .header("Authorization", "Bearer " + tokenResponse.accessToken()))
                    .andExpect(status().isConflict())
                    .andDo(print());
        }
    }

    @Nested
    class 회원_탈퇴_취소_테스트 {

        @Test
        void 회원_탈퇴_취소_성공() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            user.softDelete();

            CancelDeleteRequest request = new CancelDeleteRequest(user.getEmail(), user.getName());

            mockMvc.perform(delete("/api/users/me/deletion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(print());

            User findUser = userRepository.findById(user.getId())
                    .orElseThrow();

            assertThat(findUser.getDeletedAt()).isNull();
        }

        @Test
        void 유효하지_않은_요청_데이터_400반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");
            user.softDelete();

            CancelDeleteRequest request = new CancelDeleteRequest(null, user.getName());

            mockMvc.perform(delete("/api/users/me/deletion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        void 탈퇴되어있지않은_회원_404반환() throws Exception {
            User user = testDataFactory.createUser("테스터", "tester@email.com", "rawPassword");

            CancelDeleteRequest request = new CancelDeleteRequest(user.getEmail(), user.getName());

            mockMvc.perform(delete("/api/users/me/deletion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}