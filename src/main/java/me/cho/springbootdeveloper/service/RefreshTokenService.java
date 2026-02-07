package me.cho.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.cho.springbootdeveloper.domain.RefreshToken;
import me.cho.springbootdeveloper.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
    }

}
