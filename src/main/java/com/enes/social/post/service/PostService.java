package com.enes.social.post.service;

import com.enes.social.comment.repository.CommentRepository;
import com.enes.social.common.dto.CursorPageResponse;
import com.enes.social.common.exception.ForbiddenException;
import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.like.repository.PostLikeRepository;
import com.enes.social.post.dto.CreatePostRequest;
import com.enes.social.post.dto.PostDetailResponse;
import com.enes.social.post.dto.PostResponse;
import com.enes.social.post.dto.UpdatePostRequest;
import com.enes.social.post.model.Post;
import com.enes.social.post.repository.PostRepository;
import com.enes.social.user.model.User;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gönderi iş mantığı: CRUD, keyset (cursor) sayfalama ve sahiplik kontrolü.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponse create(Long authorId, CreatePostRequest request) {
        // Yazarı SELECT etmeden referansla bağlarız; yanıt oluşturulurken proxy tek sefer yüklenir.
        User author = userRepository.getReferenceById(authorId);
        Post post = Post.builder()
                .author(author)
                .content(request.content().trim())
                .build();
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PostDetailResponse get(Long id, Long currentUserId) {
        Post post = findOrThrow(id);
        long likeCount = postLikeRepository.countByPostId(id);
        long commentCount = commentRepository.countByPostId(id);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(id, currentUserId);
        return PostDetailResponse.from(post, likeCount, commentCount, liked);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> feed(Long cursor, Integer size) {
        int pageSize = CursorPageResponse.normalizeSize(size);
        List<Post> rows = postRepository.findFeed(cursor, Limit.of(pageSize + 1));
        return CursorPageResponse.paginate(rows, pageSize, PostResponse::from, Post::getId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> byAuthor(String username, Long cursor, Integer size) {
        User author = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + username));
        int pageSize = CursorPageResponse.normalizeSize(size);
        List<Post> rows = postRepository.findByAuthor(author.getId(), cursor, Limit.of(pageSize + 1));
        return CursorPageResponse.paginate(rows, pageSize, PostResponse::from, Post::getId);
    }

    @Transactional
    public PostResponse update(Long id, Long currentUserId, UpdatePostRequest request) {
        Post post = findOrThrow(id);
        requireOwner(post, currentUserId);
        post.setContent(request.content().trim());
        // flush: UPDATE'i şimdi yaz ki @UpdateTimestamp atansın ve yanıt taze updatedAt içersin.
        postRepository.flush();
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gönderi bulunamadı: " + id));
        requireOwner(post, currentUserId);
        postRepository.delete(post);
    }

    private Post findOrThrow(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gönderi bulunamadı: " + id));
    }

    private void requireOwner(Post post, Long currentUserId) {
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("Bu gönderi üzerinde işlem yapma yetkiniz yok");
        }
    }
}
