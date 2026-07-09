package com.enes.social.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger UI) yapılandırması. UI: /swagger-ui.html, şema: /v3/api-docs.
 *
 * <p>{@code bearerAuth} güvenlik şeması global gereksinim olarak tanımlı: Swagger UI'daki
 * "Authorize" butonuna login yanıtındaki {@code accessToken} yapıştırılarak korumalı
 * endpoint'ler denenebilir; /api/auth/** zaten token istemez.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI socialHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SocialHub API")
                        .description("Sosyal platform backend'i — JWT auth, gönderi, takip, "
                                + "feed, beğeni/yorum ve bildirim uçları. Korumalı uçlar için "
                                + "önce /api/auth/login ile token alın, sonra Authorize butonunu kullanın.")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
