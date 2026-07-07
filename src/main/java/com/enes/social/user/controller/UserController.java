package com.enes.social.user.controller;

import com.enes.social.auth.dto.UserResponse;
import com.enes.social.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /** Geçerli JWT ile giriş yapmış kullanıcının kendi profilini döner. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return UserResponse.from(principal.getDomainUser());
    }
}
