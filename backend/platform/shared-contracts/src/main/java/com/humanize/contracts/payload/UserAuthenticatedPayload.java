package com.humanize.contracts.payload;

public record UserAuthenticatedPayload(
        String userId,
        String email,
        String displayName,
        String provider
) {
}
