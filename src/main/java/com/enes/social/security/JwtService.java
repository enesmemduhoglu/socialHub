package com.enes.social.security;

import com.enes.social.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT üretimi ve doğrulaması. HS256 ile imzalanır.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.expirationMs();
        this.issuer = properties.issuer();
    }

    /** Verilen kullanıcı için imzalı access token ve son geçerlilik anını üretir. */
    public GeneratedToken generate(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMs);

        String token = Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();

        return new GeneratedToken(token, expiresAt);
    }

    /**
     * Token'ı doğrular ve içindeki username (subject) değerini döner.
     * Geçersiz/süresi dolmuş token'da {@link io.jsonwebtoken.JwtException} fırlatır.
     */
    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    /** Üretilen token ve son geçerlilik anı. */
    public record GeneratedToken(String token, Instant expiresAt) {
    }
}
