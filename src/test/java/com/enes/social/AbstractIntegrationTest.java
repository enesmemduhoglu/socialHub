package com.enes.social;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tüm entegrasyon testlerinin ortak temeli. Testler gerçek bir PostgreSQL'e
 * (compose'daki {@code socialhub_test} veritabanı, "test" profili) karşı, tam
 * Spring bağlamı ve güvenlik filtreleriyle koşar.
 *
 * <p>Test verileri kalıcı bir veritabanında biriktiğinden, çalıştırmalar arası
 * çakışmayı önlemek için kullanıcı adları hem JVM çalıştırmasına özel rastgele
 * bir önek ({@link #RUN}) hem de artan bir sayaç ile benzersizleştirilir.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final String RUN = UUID.randomUUID().toString().substring(0, 8);
    private static final AtomicLong SEQ = new AtomicLong();

    @Autowired
    protected MockMvc mockMvc;

    /** Çalıştırmalar ve testler arasında benzersiz bir kullanıcı adı üretir. */
    protected String uniqueUsername(String prefix) {
        return prefix + RUN + SEQ.incrementAndGet();
    }

    /** Verilen kullanıcı adıyla kayıt olur ve access token'ı döner. */
    protected String registerAndGetToken(String username) throws Exception {
        String body = """
                {"username":"%s","email":"%s@example.com","password":"parola1234","displayName":"%s"}
                """.formatted(username, username, username);
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
