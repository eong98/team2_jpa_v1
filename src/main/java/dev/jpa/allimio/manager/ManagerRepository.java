package dev.jpa.allimio.manager;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ManagerRepository extends JpaRepository<Manager, Long> {
  /** 아이디 중복 검사 */
  public boolean existsById(String id);
  
  /** 회원가입 (자동지원, 선언x) */
  
  /** 로그인 */
  public boolean existsByIdAndPassword(String id, String password);
  
  /** 페스워드 제외 조회 */
  @Query("SELECT new dev.jpa.allimio.manager.ManagerDTO(u.no, u.id, u.mname, u.email, u.phone, u.grade, u.status, u.udate, u.cdate) " +
      "FROM Manager u ")  
  public List<ManagerDTO> findAllWithoutPassword();
  
  /** 특정 관리자의 정보 조회 */
  @Query("SELECT new dev.jpa.allimio.manager.ManagerDTO(u.no, u.id, u.mname, u.email, u.phone, u.grade, u.status, u.udate, u.cdate) " +
      "FROM Manager u " +
      "WHERE u.no = :no")  
  public Optional<ManagerDTO> findWithNoWithoutPassword(@Param("no") Long no);
  
  /** 비밀번호 변경 */
  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value="UPDATE MANAGER SET password=:password WHERE id = :id", nativeQuery = true)
  public int updatePassword(@Param("id") String id, @Param("password") String password);
}
