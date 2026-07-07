package com.enes.social.comment.controller;

import com.enes.social.comment.dto.CommentResponse;
import com.enes.social.comment.dto.CreateCommentRequest;
import com.enes.social.comment.service.CommentService;
import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long postId,
                                  @Valid @RequestBody CreateCommentRequest request,
                                  @AuthenticationPrincipal SecurityUser principal) {
        return commentService.create(principal.getDomainUser().getId(), postId, request);
    }

    /** Gönderinin yorumları — keyset sayfalama (en yeniden eskiye). */
    @GetMapping("/{postId}/comments")
    public CursorPageResponse<CommentResponse> list(@PathVariable Long postId,
                                                    @RequestParam(required = false) Long cursor,
                                                    @RequestParam(required = false) Integer size) {
        return commentService.list(postId, cursor, size);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long postId,
                       @PathVariable Long commentId,
                       @AuthenticationPrincipal SecurityUser principal) {
        commentService.delete(commentId, principal.getDomainUser().getId());
    }
}
