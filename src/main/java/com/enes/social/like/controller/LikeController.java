package com.enes.social.like.controller;

import com.enes.social.like.service.LikeService;
import com.enes.social.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /** Gönderiyi beğen (idempotent). */
    @PostMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable Long postId,
                     @AuthenticationPrincipal SecurityUser principal) {
        likeService.like(principal.getDomainUser().getId(), postId);
    }

    /** Beğeniyi geri al (idempotent). */
    @DeleteMapping("/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long postId,
                       @AuthenticationPrincipal SecurityUser principal) {
        likeService.unlike(principal.getDomainUser().getId(), postId);
    }
}
