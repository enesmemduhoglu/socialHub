package com.enes.social.follow.controller;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.follow.service.FollowService;
import com.enes.social.security.SecurityUser;
import com.enes.social.user.dto.ProfileResponse;
import com.enes.social.user.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/users/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable String username,
                       @AuthenticationPrincipal SecurityUser principal) {
        followService.follow(principal.getDomainUser().getId(), username);
    }

    @DeleteMapping("/users/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable String username,
                         @AuthenticationPrincipal SecurityUser principal) {
        followService.unfollow(principal.getDomainUser().getId(), username);
    }

    /** Kullanıcının takipçileri — keyset sayfalama. */
    @GetMapping("/users/{username}/followers")
    public CursorPageResponse<UserSummary> followers(@PathVariable String username,
                                                     @RequestParam(required = false) Long cursor,
                                                     @RequestParam(required = false) Integer size) {
        return followService.followers(username, cursor, size);
    }

    /** Kullanıcının takip ettikleri — keyset sayfalama. */
    @GetMapping("/users/{username}/following")
    public CursorPageResponse<UserSummary> following(@PathVariable String username,
                                                     @RequestParam(required = false) Long cursor,
                                                     @RequestParam(required = false) Integer size) {
        return followService.following(username, cursor, size);
    }

    /** Kullanıcı profili: takip sayıları + isteği yapanın takip durumu. */
    @GetMapping("/users/{username}")
    public ProfileResponse profile(@PathVariable String username,
                                   @AuthenticationPrincipal SecurityUser principal) {
        return followService.profile(username, principal.getDomainUser().getId());
    }
}
