package com.humanize.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GoogleTokenVerifier {

    private final GoogleProperties googleProperties;

    public GoogleTokenVerifier(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
    }

    public GoogleUser verify(String idToken) {
        if (!StringUtils.hasText(googleProperties.getClientId())) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID is not configured for auth-service");
        }

        Builder builder = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleProperties.getClientId()));

        try {
            GoogleIdToken parsedToken = builder.build().verify(idToken);
            if (parsedToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = parsedToken.getPayload();
            Boolean emailVerified = payload.getEmailVerified();

            if (!StringUtils.hasText(payload.getEmail()) || Boolean.FALSE.equals(emailVerified)) {
                throw new IllegalArgumentException("Google account email is missing or not verified");
            }

            String displayName = payload.get("name") == null ? "Reader" : String.valueOf(payload.get("name"));
            return new GoogleUser(payload.getSubject(), payload.getEmail(), displayName);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalArgumentException("Failed to verify Google ID token", e);
        }
    }

    public record GoogleUser(String userId, String email, String displayName) {
    }
}
