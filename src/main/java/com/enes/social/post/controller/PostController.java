package com.enes.social.post.controller;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.post.dto.CreatePostRequest;
import com.enes.social.post.dto.PostDetailResponse;
import com.enes.social.post.dto.PostResponse;
import com.enes.social.post.dto.UpdatePostRequest;
import com.enes.social.post.service.PostService;
import com.enes.social.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody CreatePostRequest request,
                               @AuthenticationPrincipal SecurityUser principal) {
        return postService.create(principal.getDomainUser().getId(), request);
    }

    @GetMapping("/posts/{id}")
    public PostDetailResponse getById(@PathVariable Long id,
                                      @AuthenticationPrincipal SecurityUser principal) {
        return postService.get(id, principal.getDomainUser().getId());
    }

    /** Genel akış — keyset sayfalama. cursor boşsa en yeni gönderilerden başlar. */
    @GetMapping("/posts")
    public CursorPageResponse<PostResponse> feed(@RequestParam(required = false) Long cursor,
                                                 @RequestParam(required = false) Integer size) {
        return postService.feed(cursor, size);
    }

    /** Belirli kullanıcının gönderileri — keyset sayfalama. */
    @GetMapping("/users/{username}/posts")
    public CursorPageResponse<PostResponse> byAuthor(@PathVariable String username,
                                                     @RequestParam(required = false) Long cursor,
                                                     @RequestParam(required = false) Integer size) {
        return postService.byAuthor(username, cursor, size);
    }

    @PutMapping("/posts/{id}")
    public PostResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdatePostRequest request,
                               @AuthenticationPrincipal SecurityUser principal) {
        return postService.update(id, principal.getDomainUser().getId(), request);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal SecurityUser principal) {
        postService.delete(id, principal.getDomainUser().getId());
    }
}
