package com.humanize.auth;

import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class AuthStateRepository {
    private final AuthUserJpaRepository userRepository;
    private final AuthSessionJpaRepository sessionRepository;

    public AuthStateRepository(AuthUserJpaRepository userRepository, AuthSessionJpaRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public void recordLogin(String userId, String email, String displayName, String refreshToken) {
        Instant now = Instant.now();

        AuthUserEntity user = userRepository.findById(userId).orElseGet(AuthUserEntity::new);
        user.setUserId(userId);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setProvider("google");
        user.setLastLoginAt(now);
        userRepository.save(user);

        AuthSessionEntity session = new AuthSessionEntity();
        session.setSessionId(sessionId(userId));
        session.setUserId(userId);
        session.setRefreshToken(refreshToken);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        sessionRepository.save(session);
    }

    public void rotateSessionRefreshToken(String userId, String refreshToken) {
        Instant now = Instant.now();
        AuthSessionEntity session = sessionRepository.findById(sessionId(userId)).orElseGet(AuthSessionEntity::new);
        session.setSessionId(sessionId(userId));
        session.setUserId(userId);
        session.setRefreshToken(refreshToken);
        if (session.getCreatedAt() == null) {
            session.setCreatedAt(now);
        }
        session.setUpdatedAt(now);
        sessionRepository.save(session);
    }

    private static String sessionId(String userId) {
        return userId + "::default";
    }
}
