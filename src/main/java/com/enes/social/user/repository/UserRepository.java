package com.enes.social.user.repository;

import com.enes.social.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Kullanıcı sorguları. Benzersizlik indeksleri büyük/küçük harf duyarsız olduğu için
 * arama ve varlık kontrolleri de IgnoreCase yapılır.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
