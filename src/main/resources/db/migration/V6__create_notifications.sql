-- Bildirimler: bir kullanıcıya (recipient), bir başkasının (actor) eylemi sonucu üretilir.
-- type: FOLLOW | LIKE | COMMENT. post_id yalnızca LIKE/COMMENT için doludur.
CREATE TABLE notifications (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL,
    post_id      BIGINT      REFERENCES posts(id) ON DELETE CASCADE,
    is_read      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Alıcının bildirim akışı (recipient_id = ?, id DESC) keyset sorgusu için.
CREATE INDEX ix_notifications_recipient_id_id_desc ON notifications (recipient_id, id DESC);

-- Okunmamış sayısı için kısmi (partial) index — yalnızca okunmamışları indeksler.
CREATE INDEX ix_notifications_unread ON notifications (recipient_id) WHERE is_read = FALSE;
