package com.enes.social.feed;

import com.enes.social.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedIntegrationTest extends AbstractIntegrationTest {

    private void createPost(String token, String content) throws Exception {
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isCreated());
    }

    private void follow(String token, String target) throws Exception {
        mockMvc.perform(post("/api/users/" + target + "/follow")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void feed_containsSelfAndFollowees_excludesOthers() throws Exception {
        String alice = uniqueUsername("fa");
        String bob = uniqueUsername("fb");
        String carol = uniqueUsername("fc");
        String dave = uniqueUsername("fd");
        String aliceT = registerAndGetToken(alice);
        String bobT = registerAndGetToken(bob);
        String carolT = registerAndGetToken(carol);
        String daveT = registerAndGetToken(dave);

        follow(aliceT, bob);
        follow(aliceT, carol);

        // Oluşturma sırası (artan id): bob, carol, dave (takip edilmiyor), alice.
        createPost(bobT, "post-" + bob);
        createPost(carolT, "post-" + carol);
        createPost(daveT, "post-" + dave);
        createPost(aliceT, "post-" + alice);

        // Alice'in akışı: kendi + bob + carol; dave hariç, en yeniden eskiye.
        mockMvc.perform(get("/api/feed").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[*].content",
                        contains("post-" + alice, "post-" + carol, "post-" + bob)))
                .andExpect(jsonPath("$.hasMore", is(false)));
    }

    @Test
    void feed_cursorPagination() throws Exception {
        String alice = uniqueUsername("ga");
        String bob = uniqueUsername("gb");
        String aliceT = registerAndGetToken(alice);
        String bobT = registerAndGetToken(bob);
        follow(aliceT, bob);

        createPost(bobT, "p1-" + bob);
        createPost(aliceT, "p2-" + alice);
        createPost(bobT, "p3-" + bob);
        // Akış (yeni->eski): p3, p2, p1

        String page1 = mockMvc.perform(get("/api/feed?size=2").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].content", is("p3-" + bob)))
                .andExpect(jsonPath("$.items[1].content", is("p2-" + alice)))
                .andExpect(jsonPath("$.hasMore", is(true)))
                .andReturn().getResponse().getContentAsString();

        Number nextCursor = JsonPath.read(page1, "$.nextCursor");

        mockMvc.perform(get("/api/feed?size=2&cursor=" + nextCursor.longValue())
                        .header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].content", is("p1-" + bob)))
                .andExpect(jsonPath("$.hasMore", is(false)))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void feed_reflectsUnfollow() throws Exception {
        String alice = uniqueUsername("ha");
        String bob = uniqueUsername("hb");
        String aliceT = registerAndGetToken(alice);
        String bobT = registerAndGetToken(bob);

        follow(aliceT, bob);
        createPost(bobT, "bobpost-" + bob);

        // Takipteyken bob'un gönderisi akışta.
        mockMvc.perform(get("/api/feed").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        // Takibi bırakınca akıştan çıkar.
        mockMvc.perform(delete("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceT)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/feed").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void feed_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void feed_itemsCarryLikeAndCommentCounts() throws Exception {
        String alice = uniqueUsername("ia");
        String bob = uniqueUsername("ib");
        String aliceT = registerAndGetToken(alice);
        String bobT = registerAndGetToken(bob);
        follow(aliceT, bob);

        createPost(bobT, "sayilacak-" + bob);

        String feed = mockMvc.perform(get("/api/feed").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number postId = JsonPath.read(feed, "$.items[0].id");

        // Alice beğenir, bob yorum yapar.
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", bearer(aliceT)))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", bearer(bobT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"yorum\"}"))
                .andExpect(status().isCreated());

        // Alice'in akışında sayılar ve kendi beğeni durumu dolu gelir.
        mockMvc.perform(get("/api/feed").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].likeCount", is(1)))
                .andExpect(jsonPath("$.items[0].commentCount", is(1)))
                .andExpect(jsonPath("$.items[0].likedByCurrentUser", is(true)));

        // Bob beğenmedi: aynı gönderi onun akışında likedByCurrentUser=false döner.
        mockMvc.perform(get("/api/feed").header("Authorization", bearer(bobT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].likeCount", is(1)))
                .andExpect(jsonPath("$.items[0].likedByCurrentUser", is(false)));
    }
}
