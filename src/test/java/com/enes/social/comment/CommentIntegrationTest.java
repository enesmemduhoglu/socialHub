package com.enes.social.comment;

import com.enes.social.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentIntegrationTest extends AbstractIntegrationTest {

    private long createPost(String token, String content) throws Exception {
        String response = mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private long comment(String token, long postId, String content) throws Exception {
        String response = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    @Test
    void create_list_andCount() throws Exception {
        String alice = registerAndGetToken(uniqueUsername("ca"));
        String bob = uniqueUsername("cb");
        String bobT = registerAndGetToken(bob);
        long postId = createPost(alice, "gonderi");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", bearer(bobT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"guzel gonderi\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is("guzel gonderi")))
                .andExpect(jsonPath("$.author.username", is(bob)));

        mockMvc.perform(get("/api/posts/" + postId + "/comments").header("Authorization", bearer(bobT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].content", is("guzel gonderi")));

        // Post detayında commentCount.
        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", bearer(bobT)))
                .andExpect(jsonPath("$.commentCount", is(1)));
    }

    @Test
    void delete_onlyAuthorAllowed_andCountUpdates() throws Exception {
        String alice = registerAndGetToken(uniqueUsername("da"));
        String bobT = registerAndGetToken(uniqueUsername("db"));
        String carolT = registerAndGetToken(uniqueUsername("dc"));
        long postId = createPost(alice, "gonderi");

        long bobComment = comment(bobT, postId, "bob yorumu");
        comment(carolT, postId, "carol yorumu");

        // Carol, bob'un yorumunu silemez.
        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + bobComment)
                        .header("Authorization", bearer(carolT)))
                .andExpect(status().isForbidden());

        // Bob kendi yorumunu siler.
        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + bobComment)
                        .header("Authorization", bearer(bobT)))
                .andExpect(status().isNoContent());

        // Geriye 1 yorum kalır.
        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", bearer(bobT)))
                .andExpect(jsonPath("$.commentCount", is(1)));
    }

    @Test
    void comment_nonexistentPost_returns404() throws Exception {
        String token = registerAndGetToken(uniqueUsername("cx"));
        mockMvc.perform(post("/api/posts/999999/comments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"merhaba\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void comment_blankContent_returns400() throws Exception {
        String token = registerAndGetToken(uniqueUsername("cz"));
        long postId = createPost(token, "gonderi");
        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.content").exists());
    }

    @Test
    void comment_requiresAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"anon\"}"))
                .andExpect(status().isUnauthorized());
    }
}
