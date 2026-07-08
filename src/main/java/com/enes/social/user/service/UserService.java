package com.enes.social.user.service;

import com.enes.social.auth.dto.UserResponse;
import com.enes.social.common.exception.ResourceNotFoundException;
import com.enes.social.user.dto.UpdateProfileRequest;
import com.enes.social.user.model.User;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kullanıcı profil iş mantığı. Kimlik/parola işleri auth paketindedir;
 * burada yalnızca profil alanları (displayName, bio) yönetilir.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));

        if (request.displayName() != null) {
            user.setDisplayName(normalize(request.displayName()));
        }
        if (request.bio() != null) {
            user.setBio(normalize(request.bio()));
        }
        return UserResponse.from(user);
    }

    /** Trim'ler; boş string alanı temizleme (null) olarak yorumlanır. */
    private String normalize(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
