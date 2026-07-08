package com.enes.social.user;

import com.enes.social.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PATCH /api/users/me: profil (displayName, bio) güncelleme.
 */
class ProfileUpdateIntegrationTest extends AbstractIntegrationTest {

    @Test
    void profilGuncellenirVeKaliciOlur() throws Exception {
        String token = registerAndGetToken(uniqueUsername("profil"));

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Yeni Ad","bio":"Merhaba, ben yeni bio!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Yeni Ad"))
                .andExpect(jsonPath("$.bio").value("Merhaba, ben yeni bio!"));

        // Değişiklik kalıcı: /me aynı değerleri döner.
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Yeni Ad"))
                .andExpect(jsonPath("$.bio").value("Merhaba, ben yeni bio!"));
    }

    @Test
    void nullAlanDegismezBosStringTemizler() throws Exception {
        String token = registerAndGetToken(uniqueUsername("kismi"));

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"Kalici Ad","bio":"eski bio"}
                                """))
                .andExpect(status().isOk());

        // Yalnızca bio gönderilir: displayName değişmemeli, boş bio temizlenmeli.
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bio":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Kalici Ad"))
                .andExpect(jsonPath("$.bio").isEmpty());
    }

    @Test
    void fazlaUzunBio400Doner() throws Exception {
        String token = registerAndGetToken(uniqueUsername("uzun"));
        String longBio = "x".repeat(161);

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bio":"%s"}
                                """.formatted(longBio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bio").exists());
    }

    @Test
    void tokensizIstek401Doner() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bio":"yetkisiz"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
