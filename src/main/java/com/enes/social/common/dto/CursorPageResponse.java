package com.enes.social.common.dto;

import java.util.List;

/**
 * Keyset (cursor) tabanlı sayfa yanıtı. nextCursor bir sonraki sayfa için
 * ?cursor= parametresine geri verilir; hasMore=false ise sayfalama biter.
 */
public record CursorPageResponse<T>(
        List<T> items,
        Long nextCursor,
        boolean hasMore
) {
    public static <T> CursorPageResponse<T> of(List<T> items, Long nextCursor, boolean hasMore) {
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }
}
