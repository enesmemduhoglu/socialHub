package com.enes.social.auth.service;

import com.enes.social.auth.dto.AuthResponse;
import com.enes.social.auth.dto.LoginRequest;
import com.enes.social.auth.dto.RegisterRequest;
import com.enes.social.auth.dto.UserResponse;
import com.enes.social.common.exception.DuplicateResourceException;
import com.enes.social.security.JwtService;
import com.enes.social.security.SecurityUser;
import com.enes.social.user.model.Role;
import com.enes.social.user.model.User;
import com.enes.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Kayıt ve giriş iş mantığı; her ikisi de JWT içeren {@link AuthResponse} döner.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Bu kullanıcı adı zaten alınmış");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Bu e-posta zaten kayıtlı");
        }

        String displayName = StringUtils.hasText(request.displayName())
                ? request.displayName().trim()
                : request.username();

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(displayName)
                .role(Role.USER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        // Hatalı kimlik bilgisinde AuthenticationManager BadCredentialsException fırlatır.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));

        User user = ((SecurityUser) authentication.getPrincipal()).getDomainUser();
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        JwtService.GeneratedToken token = jwtService.generate(user);
        return AuthResponse.of(token.token(), token.expiresAt(), UserResponse.from(user));
    }
}
