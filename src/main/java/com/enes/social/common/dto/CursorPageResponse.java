package com.enes.social.common.dto;

import java.util.List;
import java.util.function.Function;

/**
 * Keyset (cursor) tabanlı sayfa yanıtı. nextCursor bir sonraki sayfa için
 * ?cursor= parametresine geri verilir; hasMore=false ise sayfalama biter.
 *
 * <p>Sayfa boyutu sınırları ve dilimleme mantığı tüm servislerde ortak olduğundan
 * burada tek yerde toplanır: {@link #normalizeSize(Integer)} + {@link #paginate}.
 */
public record CursorPageResponse<T>(
        List<T> items,
        Long nextCursor,
        boolean hasMore
) {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    public static <T> CursorPageResponse<T> of(List<T> items, Long nextCursor, boolean hasMore) {
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    /** İstenen sayfa boyutunu [1, MAX_PAGE_SIZE] aralığına indirger; boş/negatifte varsayılanı döner. */
    public static int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /**
     * pageSize+1 kayıt çekilir; fazladan gelen kayıt "daha var mı?" bilgisini verir,
     * yanıta dahil edilmez. nextCursor son öğenin id'sidir.
     *
     * @param rows        repository'den {@code Limit.of(pageSize + 1)} ile çekilen kayıtlar
     * @param mapper      entity → yanıt DTO dönüşümü
     * @param idExtractor nextCursor için son kaydın id'sini çıkarır
     */
    public static <E, T> CursorPageResponse<T> paginate(List<E> rows, int pageSize,
                                                        Function<E, T> mapper,
                                                        Function<E, Long> idExtractor) {
        boolean hasMore = rows.size() > pageSize;
        List<E> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<T> items = page.stream().map(mapper).toList();
        Long nextCursor = hasMore ? idExtractor.apply(page.get(page.size() - 1)) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }
}
