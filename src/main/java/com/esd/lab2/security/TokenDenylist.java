package com.esd.lab2.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory denylist for revoked JWT IDs.
 *
 * A token is revoked by its jti, not by the user's email.
 */
@Component
public class TokenDenylist {

    private final ConcurrentHashMap<String, Instant> revoked =
            new ConcurrentHashMap<>();

    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || expiresAt == null) {
            return;
        }
        cleanExpired();
        revoked.put(jti, expiresAt);
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }

        Instant expiresAt = revoked.get(jti);
        if (expiresAt == null) {
            return false;
        }

        if (expiresAt.isBefore(Instant.now())) {
            revoked.remove(jti);
            return false;
        }

        return true;
    }

    public void cleanExpired() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
