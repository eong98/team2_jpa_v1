package dev.jpa.allimio.member;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.history.login.LoginHistory;
import dev.jpa.allimio.history.login.LoginHistoryRepository;
import dev.jpa.allimio.history.update.UpdateHistoryDTO;
import dev.jpa.allimio.history.update.UpdateHistoryRepository;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor    // final 붙은 필드들을 파라미터로 가지는 생성자가 자동생성
public class MemberService {
  private final MemberRepository memberRepository;
  private final UpdateHistoryRepository updateHistoryRepository;
  private final LoginHistoryRepository loginHistroyRepository;
  
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
   * @return 성공이면 DTO, 실패면 빈 DTO
   */
  public Map<String, Object> login(String id, String password, String ipAddr) {
    Map<String, Object> result = new HashMap<>();
    String now = Tool.getDate();
    String tag = "[회원]";
    
    Optional<Member> memberOpt = memberRepository.findById(id);
    
 // [실패 1] 존재하지 않는 계정
    if (memberOpt.isEmpty()) {
        saveLoginLogs(id, 0, "USER_NOT_FOUND", tag+"존재하지 않는 아이디입니다.", now, ipAddr, null);
        result.put("success", false);
        result.put("message", "아이디/비밀번호가 일치하지 않습니다.");
        return result;
    }

    Member member = memberOpt.get();

    // [실패 2] 회원 상태 체크 (state: 0=탈퇴, 2=정지 예시)
    if (member.getStatus() == "0") {
        saveLoginLogs(id, 0, "ACCOUNT_DELETED", tag+"탈퇴한 회원입니다.", now, ipAddr, member);
        result.put("success", false);
        result.put("message", "탈퇴한 회원입니다.");
        return result;
    }
    
    // [실패 2] 2::사유 로 저장됨으로 상태가 2로 시작되면 작동
    if (member.getStatus() != null && member.getStatus().startsWith("2")) {
        saveLoginLogs(id, 0, "ACCOUNT_SUSPENDED", tag+"정지된 회원입니다.", now, ipAddr, member);
        result.put("success", false);
        result.put("message", "정지된 회원입니다.");
        return result;
    }

    // [실패 3] 비밀번호 불일치 (Security PasswordEncoder 사용 시 passwordEncoder.matches(password, member.getPassword())로 변경)
    if (!member.getPassword().equals(password)) {
        saveLoginLogs(id, 0, "INVALID_PASSWORD", tag+"비밀번호가 맞지 않습니다.", now, ipAddr, member);
        result.put("success", false);
        result.put("message", "아이디/비밀번호가 일치하지 않습니다.");
        return result;
    }

    // [성공] 모든 검증 통과
    saveLoginLogs(id, 1, null, null, now, ipAddr, member);
    result.put("success", true);
    result.put("user", MemberDTO.from(member)); // Member 엔티티를 DTO로 변환
    return result;
}
  
  public void saveLoginLogs(String loginId, int loginResult, String failCode, String failReason, String now, String ipAddr, Member member) {
    
    LoginHistory history = LoginHistory.builder()
        .loginId(loginId)         // 시도한 아이디
        .loginResult(loginResult) // "SUCCESS" 또는 "FAIL"
        .failCode(failCode)       // "INVALID_PASSWORD", "ACCOUNT_SUSPENDED", "USER_NOT_FOUND" 등
        .failReason(failReason)   // 상세 실패 사유 (DB 내부 확인용)
        .loginDate(now)     // 시도 일시 (Tool.getDate() 값)
        .ipAddr(ipAddr)           // IP 주소
        .mno(member)              // Member 엔티티 (존재하지 않는 계정이면 null)
        .mnno(null)
        .build();

    loginHistroyRepository.save(history);
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
    
    saveUpdateLogs(member, memberDTO, 0, null , now);
    
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
    
    saveUpdateLogs(member, memberDTO, 1, managerno , now);
    
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
      // 변경 내역을 모아둘 리스트 생성
      List<String> changedColumns = new ArrayList<>();
      List<String> oldValues = new ArrayList<>();
      List<String> newValues = new ArrayList<>();

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

          // 값이 다를 경우
          if (!oldValue.equals(newValue)) {
              changedColumns.add(fieldName.toUpperCase());

              // password 필드인 경우 지정한 문자열로 저장
              if (fieldName.equals("password")) {
                  oldValues.add("changed_password");
                  newValues.add("changed_password");
              } else {
                  oldValues.add(oldValue);
                  newValues.add(newValue);
              }
          }
      }

      // 변경된 컬럼이 있을 때만 1개의 통합 로그 저장
      if (!changedColumns.isEmpty()) {
          UpdateHistoryDTO log = UpdateHistoryDTO.builder()
              .mno(member.getNo())
              .mnno(null)
              .changedColumn(String.join("/", changedColumns))
              .oldValue(String.join("/", oldValues))
              .newValue(String.join("/", newValues))
              .changeDate(now)
              .changedBy(changedBy)
              .updtMnno(managerno)
              .build();

          updateHistoryRepository.save(log.toEntity());
      }
  } catch (Exception e) {
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


