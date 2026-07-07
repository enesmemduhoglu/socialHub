-- Gönderi beğenileri. Bir kullanıcı bir gönderiyi en fazla bir kez beğenebilir.
CREATE TABLE post_likes (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id    BIGINT      NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Idempotent beğeni: aynı (gönderi, kullanıcı) ikilisi tekrar eklenemez.
    CONSTRAINT uq_post_likes_pair UNIQUE (post_id, user_id)
);

-- Bir gönderinin beğeni sayısı/varlığı sorguları için.
CREATE INDEX ix_post_likes_post_id ON post_likes (post_id);
