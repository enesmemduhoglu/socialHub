package com.enes.social.auth;

import com.enes.social.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    private String register(String username, String email, String password) throws Exception {
        String body = """
                {"username":"%s","email":"%s","password":"%s","displayName":"%s"}
                """.formatted(username, email, password, username);
        return body;
    }

    @Test
    void register_returnsTokenAndUser() throws Exception {
        String username = uniqueUsername("auth");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register(username, username + "@example.com", "parola1234")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.username", is(username)))
                .andExpect(jsonPath("$.user.role", is("USER")));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        String username = uniqueUsername("dup");
        registerAndGetToken(username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register(username, "other-" + username + "@example.com", "parola1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void register_invalidInput_returns400WithFieldErrors() throws Exception {
        String body = """
                {"username":"a!","email":"bozuk","password":"123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.password", notNullValue()));
    }

    @Test
    void login_worksByUsernameAndEmail() throws Exception {
        String username = uniqueUsername("login");
        registerAndGetToken(username);

        String byUsername = """
                {"usernameOrEmail":"%s","password":"parola1234"}
                """.formatted(username);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(byUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())));

        String byEmail = """
                {"usernameOrEmail":"%s@example.com","password":"parola1234"}
                """.formatted(username);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(byEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username", is(username)));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String username = uniqueUsername("wrongpw");
        registerAndGetToken(username);

        String body = """
                {"usernameOrEmail":"%s","password":"yanlisparola"}
                """.formatted(username);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_requiresToken() throws Exception {
        String username = uniqueUsername("me");
        String token = registerAndGetToken(username);

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(username)));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
