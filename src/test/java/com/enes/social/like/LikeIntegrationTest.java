package com.enes.social.like;

import com.enes.social.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeIntegrationTest extends AbstractIntegrationTest {

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

    @Test
    void like_isIdempotent_andReflectedInCounts() throws Exception {
        String author = registerAndGetToken(uniqueUsername("la"));
        String liker = registerAndGetToken(uniqueUsername("ll"));
        long postId = createPost(author, "begenilecek");

        // İlk beğeni.
        mockMvc.perform(post("/api/posts/" + postId + "/like").header("Authorization", bearer(liker)))
                .andExpect(status().isNoContent());
        // Tekrar beğeni — idempotent, sayaç değişmez.
        mockMvc.perform(post("/api/posts/" + postId + "/like").header("Authorization", bearer(liker)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", bearer(liker)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount", is(1)))
                .andExpect(jsonPath("$.likedByCurrentUser", is(true)));

        // Beğenmeyen başka kullanıcı için likedByCurrentUser=false.
        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", bearer(author)))
                .andExpect(jsonPath("$.likeCount", is(1)))
                .andExpect(jsonPath("$.likedByCurrentUser", is(false)));
    }

    @Test
    void unlike_isIdempotent() throws Exception {
        String author = registerAndGetToken(uniqueUsername("ua"));
        String liker = registerAndGetToken(uniqueUsername("ul"));
        long postId = createPost(author, "gonderi");

        mockMvc.perform(post("/api/posts/" + postId + "/like").header("Authorization", bearer(liker)))
                .andExpect(status().isNoContent());
        // Geri al.
        mockMvc.perform(delete("/api/posts/" + postId + "/like").header("Authorization", bearer(liker)))
                .andExpect(status().isNoContent());
        // Tekrar geri al — idempotent.
        mockMvc.perform(delete("/api/posts/" + postId + "/like").header("Authorization", bearer(liker)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", bearer(liker)))
                .andExpect(jsonPath("$.likeCount", is(0)))
                .andExpect(jsonPath("$.likedByCurrentUser", is(false)));
    }

    @Test
    void like_nonexistentPost_returns404() throws Exception {
        String token = registerAndGetToken(uniqueUsername("lx"));
        mockMvc.perform(post("/api/posts/999999/like").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void like_requiresAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/posts/1/like"))
                .andExpect(status().isUnauthorized());
    }
}
