package com.enes.social.user.controller;

import com.enes.social.auth.dto.UserResponse;
import com.enes.social.security.SecurityUser;
import com.enes.social.user.dto.UpdateProfileRequest;
import com.enes.social.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Giriş yapmış kullanıcının profili")
public class UserController {

    private final UserService userService;

    /** Geçerli JWT ile giriş yapmış kullanıcının kendi profilini döner. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return UserResponse.from(principal.getDomainUser());
    }

    /** Kendi profilini günceller (PATCH: null alan değişmez, boş string temizler). */
    @Operation(summary = "Profil güncelle",
            description = "PATCH semantiği: null gönderilen alan değişmez, boş string alanı temizler (null yapar).")
    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal SecurityUser principal,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getDomainUser().getId(), request);
    }
}
