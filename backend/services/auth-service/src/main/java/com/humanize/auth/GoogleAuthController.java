package com.humanize.auth;

import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.UserAuthenticatedPayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class GoogleAuthController {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtTokenService jwtTokenService;
    private final AuthStateRepository authStateRepository;
    private final DomainEventPublisher eventPublisher;

    public GoogleAuthController(
            GoogleTokenVerifier googleTokenVerifier,
            JwtTokenService jwtTokenService,
            AuthStateRepository authStateRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtTokenService = jwtTokenService;
        this.authStateRepository = authStateRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/google/id-token")
    public AuthResponse loginWithGoogleIdToken(@Valid @RequestBody IdTokenLoginRequest request) {
        GoogleTokenVerifier.GoogleUser googleUser = googleTokenVerifier.verify(request.idToken());
        JwtTokenService.AuthTokens tokens = jwtTokenService.issueTokens(
                googleUser.userId(),
                googleUser.email(),
                googleUser.displayName()
        );
        authStateRepository.recordLogin(
                googleUser.userId(),
                googleUser.email(),
                googleUser.displayName(),
                tokens.refreshToken()
        );

        eventPublisher.publish(
                KafkaTopics.USER_LIFECYCLE,
                EventType.USER_AUTHENTICATED,
                googleUser.userId(),
                new UserAuthenticatedPayload(
                        googleUser.userId(),
                        googleUser.email(),
                        googleUser.displayName(),
                        "google"
                ),
                "auth-service"
        );

        return new AuthResponse(
                "authenticated",
                "google",
                googleUser.userId(),
                googleUser.email(),
                googleUser.displayName(),
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresInSeconds(),
                "Bearer"
        );
    }

    @PostMapping("/refresh")
    public Map<String, Object> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        JwtTokenService.TokenPrincipal principal = jwtTokenService.verifyRefreshAndGetPrincipal(request.refreshToken());
        JwtTokenService.AuthTokens tokens = jwtTokenService.refresh(request.refreshToken());
        authStateRepository.rotateSessionRefreshToken(principal.userId(), tokens.refreshToken());
        return Map.of(
                "accessToken", tokens.accessToken(),
                "refreshToken", tokens.refreshToken(),
                "expiresInSeconds", tokens.expiresInSeconds(),
                "tokenType", "Bearer"
        );
    }

    public record IdTokenLoginRequest(@NotBlank String idToken) {
    }

    public record RefreshTokenRequest(@NotBlank String refreshToken) {
    }

    public record AuthResponse(
            String status,
            String provider,
            String userId,
            String email,
            String displayName,
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            String tokenType
    ) {
    }
}
