package dev.jpa.allimio.member;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.tool.Tool;
import dev.jpa.allimio.member.updatelog.MemberUpdatelogDTO;
import dev.jpa.allimio.member.updatelog.MemberUpdatelogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor    // final 붙은 필드들을 파라미터로 가지는 생성자가 자동생성
public class MemberService {
  private final MemberRepository memberRepository;
  private final MemberUpdatelogRepository memberUpdatelogRepository;
  
  /**
   * 아이디 중복 체크
   * @param id
   * @return 중복된 아이디 없음 true, 중복된 아이디 있음 false
   */
  public boolean isIdAvailable(String id) {
    return !(memberRepository.existsById(id));
  }
  
  /**
   * 회원가입
   * @param memberDTO
   * @return 입력한 회원 정보를 저장
   */
  public Member save(MemberDTO memberDTO) {
    memberDTO.setCdate(Tool.getDate());
    
    return memberRepository.save(memberDTO.toEntity());
  }
  
  /**
   * 로그인
   * @param id 
   * @param password
   * @return 성공이면 true, 실패면 false
   */
  public Optional<MemberDTO> login(String id, String password) {
    boolean check = memberRepository.existsByIdAndPassword(id, password);
    
    if(check) {
      Optional<MemberDTO> memberDTO = memberRepository.longinDTO(id, password);
      
      return memberDTO;
    } else {
      return Optional.empty();
    }
  }
  
  
  /**
   * 패스워드 제외 목록 조회
   * @return 패스워드를 제외한 회원 정보
   */
  public List<MemberDTO> findAllWithoutPassword(){
    List<MemberDTO> list = memberRepository.findAllWithoutPassword();
    
    return list;
  }
  
  /**
   * 회원이 자기 정보 수정하는 경우.
   * 중간에 saveUpdateLogs 호출 해서 업데이트 로그 보존.
   * @param memberno 
   * @param memberDTO 
   */
  @Transactional
  public void updateMemberBySelf(Long memberno, MemberDTO memberDTO) {
    String now = Tool.getDate();
    Member member = memberRepository.findById(memberno).orElseThrow();
    
    saveUpdateLogs(member, memberDTO, 1, null , now);
    
    member.updateBySelf(memberDTO.getMname(), memberDTO.getPhone(), memberDTO.getEmail(), memberDTO.getZipcode(), memberDTO.getAddr(), memberDTO.getAddrDetail(), now);
  }
  
  /**
   * 관리자가 회원 정보를 수정하는 경우.
   * 중간에 saveUpdateLogs 호출 해서 업데이트 로그 보존.
   * @param memberno
   * @param memberDTO
   * @param managerno
   */
  @Transactional
  public void updateMemberByManager(Long memberno, MemberDTO memberDTO, Long managerno) {
    String now = Tool.getDate();
    Member member = memberRepository.findById(memberno).orElseThrow();
    
    saveUpdateLogs(member, memberDTO, 0, managerno , now);
    
    member.updateByManager(memberDTO.getMname(), memberDTO.getGrade(), memberDTO.getPhone(), memberDTO.getEmail(), memberDTO.getZipcode(), memberDTO.getAddr(), memberDTO.getAddrDetail(), memberDTO.getStatus(), now);
  }
  
  /**
   * 기존값과 비교해 바뀐 값이 있다면 업데이트 로그를 찍어주는 함수
   * @param member 기존 값
   * @param memberDTO 새로운 값
   * @param changedBy 변경자 유형 0: 관리자, 1: 회원
   * @param managerno 변경한 관리자 번호
   * @param now 변경한 시간
   */
  private void saveUpdateLogs(Member member, MemberDTO memberDTO, int changedBy, Long managerno, String now) {
    String [] columsToCompare = {"mname", "phone", "email", "zipcode", "addr", "addrDetail", "grade", "status"};
    
    try {
      for (String fieldName : columsToCompare) {
        // 키 : 값 형태로 가져오기 위해 필드 정보 세팅 
        Field entityField = Member.class.getDeclaredField(fieldName);
        Field dtoField = MemberDTO.class.getDeclaredField(fieldName);
        
        // 필드 수정 권한 
        entityField.setAccessible(true);
        dtoField.setAccessible(true);
        
        // 필드 안의 값의 형태가 다르므로 오브젝트로 값을 저장.
        Object oldValueObj = entityField.get(member);
        Object newValueObj = dtoField.get(memberDTO);
        
        // 바뀐 값이 null 이면 수정하지 않은 값으로 판단하고 스킵
        if (newValueObj == null) {
          continue;
        }
        
        // 값이 null인 경우 ""으로 변환
        String oldValue = (oldValueObj != null) ? oldValueObj.toString() : "";
        String newValue = newValueObj.toString();
        
        // oldValue와 newValue의 값이 다를경우 log를 생성 후 저장.
        if(!oldValue.equals(newValue)) {
          MemberUpdatelogDTO log = MemberUpdatelogDTO.builder()
              .mno(member.getNo())
              .changedColumn(fieldName.toUpperCase())
              .oldValue(oldValue)
              .newValue(newValue)
              .changeDate(now)
              .changedBy(changedBy)
              .updtMnno(managerno)
              .build();
          
          memberUpdatelogRepository.save(log.toEntity());
        }
      }
    } catch(Exception e) {
      throw new RuntimeException("로그 생성 중 오류 발생", e);
    }
  }
  
  /**
   * 비밀번호 수정
   * @param id 아이디
   * @param password 비밀번호
   * @return 수정됐다면 true, 실패했다면 false
   */
 public boolean updatePassword(String id, String password) {
   int check = memberRepository.updatePassword(id, password);
   
   return check > 0;
 }
 
 /**
  * 특정 회원 조회
  * @param memberno 조회할 회원번호
  * @return
  */
 public MemberDTO findWithNoWithoutPassword (Long memberno) {
   
   return memberRepository.findWithNoWithoutPassword(memberno).orElseThrow();
 }
}


