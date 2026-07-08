package com.enes.social.security;

import com.enes.social.user.model.Role;
import com.enes.social.user.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtService birim testleri: Spring bağlamı olmadan token üretimi ve
 * doğrulamanın uç durumları (süre aşımı, imza, format, issuer).
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256-signing!!";
    private static final String OTHER_SECRET = "another-secret-key-that-is-also-long-enough-for-hs256!!";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private final JwtService jwtService = service(SECRET, ONE_HOUR_MS, "socialhub");

    private static JwtService service(String secret, long expirationMs, String issuer) {
        return new JwtService(new JwtProperties(secret, expirationMs, issuer));
    }

    private static User user(String username) {
        return User.builder()
                .id(42L)
                .username(username)
                .email(username + "@example.com")
                .passwordHash("irrelevant")
                .role(Role.USER)
                .build();
    }

    @Test
    void gecerliTokenRoundTrip() {
        JwtService.GeneratedToken generated = jwtService.generate(user("ayse"));

        assertThat(jwtService.extractUsername(generated.token())).isEqualTo("ayse");
        assertThat(generated.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void suresiDolmusTokenReddedilir() {
        // Negatif geçerlilik süresi: token üretildiği anda süresi dolmuştur.
        JwtService expiredIssuer = service(SECRET, -1_000L, "socialhub");
        String token = expiredIssuer.generate(user("mehmet")).token();

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void farkliSecretIleImzalanmisTokenReddedilir() {
        String token = service(OTHER_SECRET, ONE_HOUR_MS, "socialhub")
                .generate(user("fatma")).token();

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void kurcalanmisTokenReddedilir() {
        String token = jwtService.generate(user("ali")).token();
        // Payload bölümünün ortasındaki bir karakteri değiştir (header.payload.signature).
        String[] parts = token.split("\\.");
        char[] payload = parts[1].toCharArray();
        int mid = payload.length / 2;
        payload[mid] = payload[mid] == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + new String(payload) + "." + parts[2];

        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void bozukFormatliStringReddedilir() {
        assertThatThrownBy(() -> jwtService.extractUsername("bu-bir-jwt-degil"))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void yanlisIssuerliTokenReddedilir() {
        String token = service(SECRET, ONE_HOUR_MS, "baska-uygulama")
                .generate(user("veli")).token();

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(JwtException.class);
    }
}
