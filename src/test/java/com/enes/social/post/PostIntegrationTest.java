package com.enes.social.post;

import com.enes.social.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostIntegrationTest extends AbstractIntegrationTest {

    private long createPost(String token, String content) throws Exception {
        String body = """
                {"content":"%s"}
                """.formatted(content);
        String response = mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    @Test
    void createAndGetPost() throws Exception {
        String user = uniqueUsername("poster");
        String token = registerAndGetToken(user);
        long id = createPost(token, "merhaba dunya");

        mockMvc.perform(get("/api/posts/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.content", is("merhaba dunya")))
                .andExpect(jsonPath("$.author.username", is(user)));
    }

    @Test
    void cursorPagination_overAuthorTimeline() throws Exception {
        String user = uniqueUsername("pager");
        String token = registerAndGetToken(user);
        long id1 = createPost(token, "birinci");
        long id2 = createPost(token, "ikinci");
        long id3 = createPost(token, "ucuncu");

        // İlk sayfa: en yeni 2 gönderi (id3, id2), devamı var.
        String page1 = mockMvc.perform(get("/api/users/" + user + "/posts?size=2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id", is((int) id3)))
                .andExpect(jsonPath("$.items[1].id", is((int) id2)))
                .andExpect(jsonPath("$.hasMore", is(true)))
                .andExpect(jsonPath("$.nextCursor", is((int) id2)))
                .andReturn().getResponse().getContentAsString();

        Number nextCursor = JsonPath.read(page1, "$.nextCursor");

        // İkinci sayfa: kalan tek gönderi (id1), devamı yok.
        mockMvc.perform(get("/api/users/" + user + "/posts?size=2&cursor=" + nextCursor.longValue())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is((int) id1)))
                .andExpect(jsonPath("$.hasMore", is(false)))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void update_onlyOwnerAllowed() throws Exception {
        String owner = uniqueUsername("owner");
        String other = uniqueUsername("other");
        String ownerToken = registerAndGetToken(owner);
        String otherToken = registerAndGetToken(other);
        long id = createPost(ownerToken, "orijinal");

        String update = """
                {"content":"guncellendi"}
                """;

        // Sahibi güncelleyebilir.
        mockMvc.perform(put("/api/posts/" + id)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("guncellendi")));

        // Başkası güncelleyemez.
        mockMvc.perform(put("/api/posts/" + id)
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_thenNotFound() throws Exception {
        String user = uniqueUsername("deleter");
        String token = registerAndGetToken(user);
        long id = createPost(token, "silinecek");

        mockMvc.perform(delete("/api/posts/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_blankContent_returns400() throws Exception {
        String user = uniqueUsername("blank");
        String token = registerAndGetToken(user);

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.content").exists());
    }

    @Test
    void create_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"anon\"}"))
                .andExpect(status().isUnauthorized());
    }
}
