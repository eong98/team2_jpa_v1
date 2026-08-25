package dev.jpa.allimio.member.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRefreshTokenRepository extends JpaRepository<MemberRefreshToken, Long> {
  Optional<MemberRefreshToken> findByToken(String token);

}
