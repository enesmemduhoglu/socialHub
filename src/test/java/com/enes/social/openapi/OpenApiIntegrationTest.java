package com.enes.social.openapi;

import com.enes.social.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenAPI dokümantasyonu uçlarının token'sız erişilebilir ve şemanın dolu
 * olduğunu doğrular.
 */
class OpenApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void apiDocsErisilebilirVeEndpointleriIceriyor() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("SocialHub API"))
                .andExpect(jsonPath("$.paths['/api/posts']").value(notNullValue()))
                .andExpect(jsonPath("$.paths['/api/auth/login']").value(notNullValue()))
                .andExpect(jsonPath("$.paths['/api/feed']").value(notNullValue()))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void swaggerUiTokensizAcilabiliyor() throws Exception {
        // /swagger-ui.html gerçek UI sayfasına yönlendirir; 401 dönmemesi yeterli.
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
