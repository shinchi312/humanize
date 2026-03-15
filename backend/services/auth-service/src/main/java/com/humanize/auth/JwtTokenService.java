package com.humanize.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtTokenService {

    private static final String TOKEN_USE_CLAIM = "token_use";
    private static final String TOKEN_USE_ACCESS = "access";
    private static final String TOKEN_USE_REFRESH = "refresh";

    private final AuthProperties authProperties;

    public JwtTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public AuthTokens issueTokens(String userId, String email, String displayName) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(authProperties.getAccessTokenMinutes() * 60);
        Instant refreshExpiry = now.plusSeconds(authProperties.getRefreshTokenDays() * 24 * 60 * 60);

        Algorithm algorithm = Algorithm.HMAC256(secret());

        String accessToken = JWT.create()
                .withIssuer(authProperties.getIssuer())
                .withSubject(userId)
                .withClaim("email", email)
                .withClaim("name", displayName)
                .withClaim(TOKEN_USE_CLAIM, TOKEN_USE_ACCESS)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(accessExpiry))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withIssuer(authProperties.getIssuer())
                .withSubject(userId)
                .withClaim("email", email)
                .withClaim("name", displayName)
                .withClaim(TOKEN_USE_CLAIM, TOKEN_USE_REFRESH)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(refreshExpiry))
                .sign(algorithm);

        return new AuthTokens(accessToken, refreshToken, authProperties.getAccessTokenMinutes() * 60);
    }

    public AuthTokens refresh(String refreshToken) {
        DecodedJWT decoded = verifyRefreshToken(refreshToken);
        return issueTokens(
                decoded.getSubject(),
                decoded.getClaim("email").asString(),
                decoded.getClaim("name").asString()
        );
    }

    public TokenPrincipal verifyRefreshAndGetPrincipal(String refreshToken) {
        DecodedJWT decoded = verifyRefreshToken(refreshToken);
        return new TokenPrincipal(
                decoded.getSubject(),
                decoded.getClaim("email").asString(),
                decoded.getClaim("name").asString()
        );
    }

    private JWTVerifier refreshVerifier() {
        return JWT.require(Algorithm.HMAC256(secret()))
                .withIssuer(authProperties.getIssuer())
                .withClaim(TOKEN_USE_CLAIM, TOKEN_USE_REFRESH)
                .build();
    }

    private DecodedJWT verifyRefreshToken(String refreshToken) {
        return refreshVerifier().verify(refreshToken);
    }

    private String secret() {
        if (!StringUtils.hasText(authProperties.getJwtSecret())) {
            throw new IllegalStateException("AUTH_JWT_SECRET is not configured");
        }
        return authProperties.getJwtSecret();
    }

    public record AuthTokens(
            String accessToken,
            String refreshToken,
            long expiresInSeconds
    ) {
    }

    public record TokenPrincipal(
            String userId,
            String email,
            String displayName
    ) {
    }
}
