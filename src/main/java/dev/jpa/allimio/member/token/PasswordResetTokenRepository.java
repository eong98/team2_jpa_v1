package dev.jpa.allimio.member.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  /** 토큰 조회 */
  Optional<PasswordResetToken> findByToken(String token);

}
