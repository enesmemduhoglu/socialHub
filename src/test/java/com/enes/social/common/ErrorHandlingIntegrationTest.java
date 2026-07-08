package com.enes.social.common;

import com.enes.social.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler'ın çerçeve kaynaklı hataları (bozuk JSON, tip uyuşmazlığı,
 * eşleşmeyen path) 500 yerine doğru 4xx yanıtlarına çevirdiğini doğrular.
 */
class ErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void bozukJsonGovdesi400Doner() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bozuk json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("İstek gövdesi okunamadı (geçersiz JSON)"));
    }

    @Test
    void tipUyusmayanPathDegiskeni400Doner() throws Exception {
        String token = registerAndGetToken(uniqueUsername("err"));
        mockMvc.perform(get("/api/posts/abc")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Geçersiz parametre değeri: id"));
    }

    @Test
    void eslesmeyenPath404Doner() throws Exception {
        String token = registerAndGetToken(uniqueUsername("err"));
        mockMvc.perform(get("/api/boyle-bir-kaynak-yok")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
