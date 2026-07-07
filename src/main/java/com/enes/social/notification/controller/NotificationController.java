package com.enes.social.notification.controller;

import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.notification.dto.NotificationResponse;
import com.enes.social.notification.dto.UnreadCountResponse;
import com.enes.social.notification.service.NotificationService;
import com.enes.social.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Giriş yapan kullanıcının bildirimleri — keyset sayfalama (en yeniden eskiye). */
    @GetMapping
    public CursorPageResponse<NotificationResponse> list(@RequestParam(required = false) Long cursor,
                                                         @RequestParam(required = false) Integer size,
                                                         @AuthenticationPrincipal SecurityUser principal) {
        return notificationService.list(principal.getDomainUser().getId(), cursor, size);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal SecurityUser principal) {
        return new UnreadCountResponse(notificationService.unreadCount(principal.getDomainUser().getId()));
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable Long id,
                         @AuthenticationPrincipal SecurityUser principal) {
        notificationService.markRead(id, principal.getDomainUser().getId());
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal SecurityUser principal) {
        notificationService.markAllRead(principal.getDomainUser().getId());
    }
}
