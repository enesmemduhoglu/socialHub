package com.enes.social.feed.controller;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.feed.service.FeedService;
import com.enes.social.post.dto.PostResponse;
import com.enes.social.security.SecurityUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Kişisel akış: kendi + takip edilenlerin gönderileri")
public class FeedController {

    private final FeedService feedService;

    /** Giriş yapan kullanıcının kişisel akışı — keyset sayfalama. */
    @GetMapping("/feed")
    public CursorPageResponse<PostResponse> feed(@RequestParam(required = false) Long cursor,
                                                 @RequestParam(required = false) Integer size,
                                                 @AuthenticationPrincipal SecurityUser principal) {
        return feedService.timeline(principal.getDomainUser().getId(), cursor, size);
    }
}
