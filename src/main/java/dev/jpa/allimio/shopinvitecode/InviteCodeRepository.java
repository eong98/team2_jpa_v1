package dev.jpa.allimio.shopinvitecode;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
  
  public Optional<InviteCode> findByCode(String code);
}
