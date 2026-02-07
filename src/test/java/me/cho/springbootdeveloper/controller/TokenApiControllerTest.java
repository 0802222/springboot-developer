package me.cho.springbootdeveloper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import me.cho.springbootdeveloper.config.jwt.JwtFactory;
import me.cho.springbootdeveloper.config.jwt.JwtProperties;
import me.cho.springbootdeveloper.domain.RefreshToken;
import me.cho.springbootdeveloper.domain.User;
import me.cho.springbootdeveloper.dto.CreateAccessTokenRequest;
import me.cho.springbootdeveloper.repository.RefreshTokenRepository;
import me.cho.springbootdeveloper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .build();
        userRepository.deleteAll();
    }

    @DisplayName("createNewAccessToken: 새로운 액세스 토큰을 발급한다.")
    @Test
    public void createNewAccessToken() throws Exception {

        // given - 테스트 유저 생성, refreshToken 생성, DB 저장, 토큰 생성 API 요청 본문에 리프레시 토큰을 포함하여 요청객체 생성
        final String url = "/api/token";

        User testUser = userRepository.save(User.builder()
            .email("test@test.com")
            .password("test")
            .build());

        String refreshToken = JwtFactory.builder()
            .claims(Map.of("id", testUser.getId()))
            .build()
            .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);

        final String requestBody = objectMapper.writeValueAsString(request);

        // when - 토큰 추가 API 에 요청 전송, Json 타입, given 절에서 만든 객체를 요청본문으로 함께보내기
        ResultActions resultActions = mockMvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody));

        // then - 응답 코드가 201 created 인지 확인하고 응답으로 온 액세스 토큰이 비어있지않은지 확인
        resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

}