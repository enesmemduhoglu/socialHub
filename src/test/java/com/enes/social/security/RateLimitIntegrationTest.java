package com.enes.social.security;

import com.enes.social.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /api/auth/** rate limit davranışı. Genel test profili sınırı kapattığından
 * (application-test.yml) burada küçük bir kapasiteyle ayrıca açılır; uzun
 * refill süresi test boyunca kovanın dolmamasını garanti eder.
 */
@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.capacity=3",
        "app.rate-limit.refill-period=10m"
})
class RateLimitIntegrationTest extends AbstractIntegrationTest {

    @Test
    void kapasiteAsiminda429Doner() throws Exception {
        String body = """
                {"usernameOrEmail":"olmayan-kullanici","password":"yanlis-parola"}
                """;

        // Kapasite dahilindeki istekler normal akışta ilerler (401: hatalı kimlik).
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        // Kapasite tükendi: istek controller'a ulaşmadan 429 ile kesilir.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
