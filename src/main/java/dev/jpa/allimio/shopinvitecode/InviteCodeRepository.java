package dev.jpa.allimio.shopinvitecode;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
  /** 초대 코드 검사 */
  Optional<InviteCode> findByCode(String code);
  
  /** 중복 코드 검사 */
  boolean existsByCode(String code);
  
  Optional<InviteCode> findBySno(Long sno);
  
  void deleteByCode(String code);
}
