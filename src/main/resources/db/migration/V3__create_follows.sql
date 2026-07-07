-- Takip ilişkisi: users tablosu üzerinde self-referencing çoktan-çoğa bağlantı.
-- follower_id, followee_id'yi takip eder.
CREATE TABLE follows (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    follower_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followee_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Aynı takip iki kez oluşturulamaz.
    CONSTRAINT uq_follows_pair UNIQUE (follower_id, followee_id),
    -- Kullanıcı kendini takip edemez.
    CONSTRAINT ck_follows_no_self CHECK (follower_id <> followee_id)
);

-- "X'in takipçileri" (followee_id = X, id DESC) keyset sorgusu için.
CREATE INDEX ix_follows_followee_id ON follows (followee_id, id DESC);
-- "X'in takip ettikleri" (follower_id = X, id DESC) keyset sorgusu için.
CREATE INDEX ix_follows_follower_id ON follows (follower_id, id DESC);
