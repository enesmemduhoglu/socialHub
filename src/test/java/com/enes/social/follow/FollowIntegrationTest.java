package com.enes.social.follow;

import com.enes.social.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FollowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void follow_updatesProfileCountsAndLists() throws Exception {
        String alice = uniqueUsername("alice");
        String bob = uniqueUsername("bob");
        String aliceToken = registerAndGetToken(alice);
        registerAndGetToken(bob);

        // Alice, Bob'u takip eder.
        mockMvc.perform(post("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());

        // Bob'un profili: 1 takipçi, 0 takip, alice tarafından takip ediliyor.
        mockMvc.perform(get("/api/users/" + bob).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followerCount", is(1)))
                .andExpect(jsonPath("$.followingCount", is(0)))
                .andExpect(jsonPath("$.followedByCurrentUser", is(true)));

        // Alice'in kendi profili: 0 takipçi, 1 takip; kendini takip etmiyor.
        mockMvc.perform(get("/api/users/" + alice).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followerCount", is(0)))
                .andExpect(jsonPath("$.followingCount", is(1)))
                .andExpect(jsonPath("$.followedByCurrentUser", is(false)));

        // Bob'un takipçileri arasında alice var.
        mockMvc.perform(get("/api/users/" + bob + "/followers")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].username", is(alice)));

        // Alice'in takip ettikleri arasında bob var.
        mockMvc.perform(get("/api/users/" + alice + "/following")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].username", is(bob)));
    }

    @Test
    void follow_self_returns400() throws Exception {
        String user = uniqueUsername("selffollow");
        String token = registerAndGetToken(user);

        mockMvc.perform(post("/api/users/" + user + "/follow")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void follow_duplicate_returns409() throws Exception {
        String alice = uniqueUsername("dupa");
        String bob = uniqueUsername("dupb");
        String aliceToken = registerAndGetToken(alice);
        registerAndGetToken(bob);

        mockMvc.perform(post("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isConflict());
    }

    @Test
    void unfollow_removesRelation_andIsNotFoundWhenAbsent() throws Exception {
        String alice = uniqueUsername("unfa");
        String bob = uniqueUsername("unfb");
        String aliceToken = registerAndGetToken(alice);
        registerAndGetToken(bob);

        mockMvc.perform(post("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());

        // Artık takip yok → tekrar bırakma 404.
        mockMvc.perform(delete("/api/users/" + bob + "/follow")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNotFound());

        // Bob'un takipçisi kalmadı.
        mockMvc.perform(get("/api/users/" + bob).header("Authorization", bearer(aliceToken)))
                .andExpect(jsonPath("$.followerCount", is(0)))
                .andExpect(jsonPath("$.followedByCurrentUser", is(false)));
    }

    @Test
    void follow_nonexistentUser_returns404() throws Exception {
        String user = uniqueUsername("ghost");
        String token = registerAndGetToken(user);

        mockMvc.perform(post("/api/users/yokboyle_kullanici_zzz/follow")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }
}
