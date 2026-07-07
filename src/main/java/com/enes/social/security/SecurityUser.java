package com.enes.social.security;

import com.enes.social.user.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Domain {@link User}'ı Spring Security'nin beklediği {@link UserDetails} arayüzüne uyarlar.
 */
public class SecurityUser implements UserDetails {

    @Getter
    private final User domainUser;

    public SecurityUser(User domainUser) {
        this.domainUser = domainUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + domainUser.getRole().name()));
    }

    @Override
    public String getPassword() {
        return domainUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return domainUser.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return domainUser.isEnabled();
    }
}
