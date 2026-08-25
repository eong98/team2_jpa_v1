package dev.jpa.allimio.manager.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRefreshTokenRepository extends JpaRepository<ManagerRefreshToken, Long> {
  Optional<ManagerRefreshToken> findByToken(String token);
}
