package me.cho.springbootdeveloper.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import me.cho.springbootdeveloper.domain.User;
import me.cho.springbootdeveloper.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@SpringBootTest
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    // 1. generateToken() 검증 테스트
    @DisplayName("generateToken(): 유저 정보와 만료기간을 전달해 토큰을 만들 수 있다.")
    @Test
    void generateToken() {
        // given - 토큰에 추가할 유저 정보
        User testUser = userRepository.save(User.builder()
            .email("test@test.com")
            .password("test")
            .build());

        // when - 토큰 생성 (만료 기한 포함)
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // then - 검증
        Long userId = Jwts.parser()
            .setSigningKey(jwtProperties.getSecretKey()) // 복호화
            .parseClaimsJws(token) // 내용 가져오기
            .getBody()
            .get("id", Long.class);

        // 토큰 생성시 클레임으로 넣어둔 id 와 userId 가 동일한지 확인
        assertThat(userId).isEqualTo(testUser.getId());
    }

    // 2. validToken() 검증 테스트
    @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
    @Test
    void validToken_invalidToken() {
        // given - 토큰을 생성
        String token = JwtFactory.builder()
            // 이미 시간이 만료된 토큰으로 생성
            .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
            .build()
            .createToken(jwtProperties);

        // when - 유효한 토큰인지 검증 메서드를 호출해서 검증
        boolean result = tokenProvider.validToken(token);

        // then - 유효하지 않음을 확인
        assertThat(result).isFalse();
    }

    @DisplayName("validToken() : 유효한 토큰인 때에 유효성 검증에 성공한다.")
    @Test
    void validToken_validToken() {
        // given - 토큰 생성 (만료시간은 토큰 생성시 기본으로 14일 이후로 설정됨)
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then - 유효함을 확인
        assertThat(result).isTrue();
    }

    // 3. getAuthentication() 검증 테스트
    @DisplayName("getAuthentication(): 토큰 기반으로 인증정보를 가져올 수 있다.")
    @Test
    void getAuthentication() {
        // given - userEmail 값을 사용해 토큰 생성
        String userEmail = "test@test.com";
        String token = JwtFactory.builder()
            .subject(userEmail)
            .build()
            .createToken(jwtProperties);

        // when - 토큰제공자의 메서드를 호출해 인증객체를 반환 받음
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then - 반환받은 인증객체의 유저이름과 서브젝트값이 동일한지 검증
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(
            userEmail);
    }

    // 4. getUserId() 검증 테스트
    @DisplayName("getUserId() : 토큰으로 유저 ID 를 가져올 수 있다.")
    @Test
    void getUserId() {
        // given - 클레임에 id를 추가해서 토큰 생성
        Long userId = 1L;
        String token = JwtFactory.builder()
            .claims(Map.of("id", userId))
            .build()
            .createToken(jwtProperties);

        // when - 토큰제공자의 메서드를 호출해 userId를 반환 받음
        Long userIdByToken = tokenProvider.getUserId(token);

        // then - 반환받은 Id 와 UserId 가 동일한지 확인
        assertThat(userIdByToken).isEqualTo(userId);
    }

}
