package dev.jpa.allimio.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, Long> {
  
  /** 아이디 중복 검사 */
  public boolean existsById(String id);
  
  /** 회원가입 (자동지원, 선언x) */
  
  /** 로그인 */
  public boolean existsByIdAndPassword(String id, String password);
  
  /** 페스워드 제외 조회 */
  @Query("SELECT new dev.jpa.allimio.member.MemberDTO(u.no, u.id, u.email, u.phone, u.mname, u.zipcode, u.addr, u.addrDetail, u.grade, u.status, u.udate, u.cdate, u.nation) " +
      "FROM Member u ")  
  public List<MemberDTO> findAllWithoutPassword();
  
  /** 비밀번호 변경 */
  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value="UPDATE MEMBER SET password=:password WHERE id = :id", nativeQuery = true)
  public int updatePassword(@Param("id") String id, @Param("password") String password);


  /** 특정 회원의 정보 조회 */
  @Query("SELECT new dev.jpa.allimio.member.MemberDTO(u.no, u.id, u.email, u.phone, u.mname, u.zipcode, u.addr, u.addrDetail, u.grade, u.status, u.udate, u.cdate, u.nation) " +
         "FROM Member u " +
         "WHERE u.no = :no") 
  public Optional<MemberDTO> findWithNoWithoutPassword(@Param("no") Long no);
  
  /** 특정 회원의 정보 조회 */
  @Query("SELECT new dev.jpa.allimio.member.MemberDTO(u.no, u.id, u.email, u.phone, u.mname, u.zipcode, u.addr, u.addrDetail, u.grade, u.status, u.udate, u.cdate, u.nation) " +
         "FROM Member u " +
         "WHERE u.id = :id AND u.password = :password") 
  public Optional<MemberDTO> longinDTO(@Param("id") String id, @Param("password") String password);

}
