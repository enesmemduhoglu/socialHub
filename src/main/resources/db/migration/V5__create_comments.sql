-- Gönderilere yapılan yorumlar.
CREATE TABLE comments (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id    BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Bir gönderinin yorumlarını keyset (post_id = ?, id DESC) sayfalamak için.
CREATE INDEX ix_comments_post_id_id_desc ON comments (post_id, id DESC);
