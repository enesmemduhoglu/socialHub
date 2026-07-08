-- FK kolonlarını destekleyen eksik indeksler: ON DELETE CASCADE silmelerinde
-- (kullanıcı/gönderi silinince) sıralı tarama yerine indeks kullanılmasını sağlar.
CREATE INDEX ix_comments_author_id ON comments (author_id);
CREATE INDEX ix_post_likes_user_id ON post_likes (user_id);
CREATE INDEX ix_notifications_actor_id ON notifications (actor_id);
CREATE INDEX ix_notifications_post_id ON notifications (post_id);
