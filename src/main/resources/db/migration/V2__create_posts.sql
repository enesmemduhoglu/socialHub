-- Gönderi (post) tablosu: bir kullanıcının paylaştığı kısa metin içeriği.
CREATE TABLE posts (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Kullanıcı zaman tünelinin keyset (cursor) sayfalaması için:
-- WHERE author_id = ? AND id < ? ORDER BY id DESC  sorgusunu tek indeksle karşılar.
CREATE INDEX ix_posts_author_id_id_desc ON posts (author_id, id DESC);

-- Genel akışın (WHERE id < ? ORDER BY id DESC) keyset sorgusu PK indeksiyle karşılanır.
