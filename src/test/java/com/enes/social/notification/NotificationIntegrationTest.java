package com.enes.social.notification;

import com.enes.social.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationIntegrationTest extends AbstractIntegrationTest {

    private long createPost(String token, String content) throws Exception {
        String response = mockMvc.perform(post("/api/posts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long unreadCount(String token) throws Exception {
        String response = mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.unreadCount")).longValue();
    }

    @Test
    void follow_createsNotificationForFollowee() throws Exception {
        String alice = uniqueUsername("na");
        String aliceT = registerAndGetToken(alice);
        String bob = uniqueUsername("nb");
        String bobT = registerAndGetToken(bob);

        mockMvc.perform(post("/api/users/" + alice + "/follow")
                        .header("Authorization", bearer(bobT)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications").header("Authorization", bearer(aliceT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].type", is("FOLLOW")))
                .andExpect(jsonPath("$.items[0].actor.username", is(bob)))
                .andExpect(jsonPath("$.items[0].read", is(false)))
                .andExpect(jsonPath("$.items[0].postId").doesNotExist());
    }

    @Test
    void like_createsNotificationForPostAuthor() throws Exception {
        String aliceT = registerAndGetToken(uniqueUsername("la"));
        String bob = uniqueUsername("lb");
        String bobT = registerAndGetToken(bob);
        long postId = createPost(aliceT, "gonderi");

        mockMvc.perform(post("/api/posts/" + postId + "/like").header("Authorization", bearer(bobT)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications").header("Authorization", bearer(aliceT)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].type", is("LIKE")))
                .andExpect(jsonPath("$.items[0].actor.username", is(bob)))
                .andExpect(jsonPath("$.items[0].postId", is((int) postId)));
    }

    @Test
    void comment_createsNotificationForPostAuthor() throws Exception {
        String aliceT = registerAndGetToken(uniqueUsername("ka"));
        String bob = uniqueUsername("kb");
        String bobT = registerAndGetToken(bob);
        long postId = createPost(aliceT, "gonderi");

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", bearer(bobT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"yorum\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifications").header("Authorization", bearer(aliceT)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].type", is("COMMENT")))
                .andExpect(jsonPath("$.items[0].actor.username", is(bob)))
                .andExpect(jsonPath("$.items[0].postId", is((int) postId)));
    }

    @Test
    void selfActions_produceNoNotification() throws Exception {
        String aliceT = registerAndGetToken(uniqueUsername("sa"));
        long postId = createPost(aliceT, "kendi gonderim");

        mockMvc.perform(post("/api/posts/" + postId + "/like").header("Authorization", bearer(aliceT)))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", bearer(aliceT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"kendi yorumum\"}"))
                .andExpect(status().isCreated());

        // Kendi eylemlerinden bildirim gelmez.
        org.assertj.core.api.Assertions.assertThat(unreadCount(aliceT)).isZero();
        mockMvc.perform(get("/api/notifications").header("Authorization", bearer(aliceT)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void markRead_and_markAllRead() throws Exception {
        String alice = uniqueUsername("ma");
        String aliceT = registerAndGetToken(alice);
        String bobT = registerAndGetToken(uniqueUsername("mb"));
        long postId = createPost(aliceT, "gonderi");

        // bob, alice'i takip eder ve gönderisini beğenir -> 2 bildirim.
        mockMvc.perform(post("/api/users/" + alice + "/follow")
                .header("Authorization", bearer(bobT))).andExpect(status().isNoContent());
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                .header("Authorization", bearer(bobT))).andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(unreadCount(aliceT)).isEqualTo(2);

        // İlk bildirimi okundu işaretle.
        String list = mockMvc.perform(get("/api/notifications").header("Authorization", bearer(aliceT)))
                .andReturn().getResponse().getContentAsString();
        long firstId = ((Number) JsonPath.read(list, "$.items[0].id")).longValue();

        mockMvc.perform(post("/api/notifications/" + firstId + "/read").header("Authorization", bearer(aliceT)))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(unreadCount(aliceT)).isEqualTo(1);

        // Tümünü okundu işaretle.
        mockMvc.perform(post("/api/notifications/read-all").header("Authorization", bearer(aliceT)))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(unreadCount(aliceT)).isZero();
    }

    @Test
    void notifications_areRecipientScoped() throws Exception {
        String alice = uniqueUsername("ra");
        String aliceT = registerAndGetToken(alice);
        String bobT = registerAndGetToken(uniqueUsername("rb"));

        mockMvc.perform(post("/api/users/" + alice + "/follow")
                .header("Authorization", bearer(bobT))).andExpect(status().isNoContent());

        // Bildirim alice'e ait; bob'un listesi boş.
        mockMvc.perform(get("/api/notifications").header("Authorization", bearer(bobT)))
                .andExpect(jsonPath("$.items", hasSize(0)));
        org.assertj.core.api.Assertions.assertThat(unreadCount(bobT)).isZero();
    }

    @Test
    void notifications_requireAuth() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
