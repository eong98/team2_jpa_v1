package dev.jpa.allimio.member;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class MemberCont {
  private final MemberService memberService;

  // 임시 DTO 생성
  public record IdCheckResponse(boolean available) {
  }

  /**
   * 아이디 중복 체크 http://localhost/v1/member/check/{id}
   * 
   * @param id
   * @return 중복된 아이디 없음 true, 중복된 아이디 있음 false
   */
  @GetMapping(path = "/check/{id}")
  public ResponseEntity<IdCheckResponse> isIdAvailable(@PathVariable("id") String id) {
    boolean available = memberService.isIdAvailable(id);

    return ResponseEntity.ok(new IdCheckResponse(available));
  }

  /**
   * 회원가입 http://10.1.205.120:9102/v1/user/save
   * 
   * @param memberDTO
   * @return 가입정보
   */
  @PostMapping(path = "/save")
  public ResponseEntity<Member> saveMember(@Valid @RequestBody MemberDTO memberDTO) {
    Member savedEntity = memberService.save(memberDTO);

    return ResponseEntity.ok(savedEntity);
  }

  /**
   * 모든 회원의 비밀번호를 제외한 정보 조회
   * http://10.1.205.120:9102/v1/member/find
   * @return 비밀번호를 제외한 정보
   */
  @GetMapping(path = "/find")
  public ResponseEntity<List<MemberDTO>> findAllWithoutPassword() {
    List<MemberDTO> list = memberService.findAllWithoutPassword();

    return ResponseEntity.ok(list);
  }
  
  /**
   * 특정 회원의 정보 조회
   * http://10.1.205.120:9102/v1/member/find/{no}
   * @param memberno 조회할 회원의 회원번호
   * @return 특정 회원의 정보
   */
  @GetMapping(path = "/find/{memberno}")
  public ResponseEntity<MemberDTO> findWithNoWithoutPassword(@PathVariable(name = "memberno") Long memberno){
    MemberDTO memberDTO = memberService.findWithNoWithoutPassword(memberno);
    
    return ResponseEntity.ok(memberDTO);
  }

  /**
   * 로그인
   * @param id
   * @param password
   * @return 성공이면 true, 실패면 false
   */
  @PostMapping(path="/login")
 public ResponseEntity<Boolean> login(
     @RequestParam(name="id", defaultValue="") String id, 
     @RequestParam(name="password", defaultValue="") String password){
    boolean check = memberService.login(id, password);
    
    return ResponseEntity.ok(check);
  }

  /**
   * 회원이 자신을 수정한 경우
   * @param memberno 회원번호
   * @param memberDTO 변경한 값
   * @return 성공시 200 OK 
   */
  @PutMapping(path="/update/self/{memberno}")
  public ResponseEntity<Void> updateMemberBySelf(
      @PathVariable("memberno") Long memberno,
      @RequestBody MemberDTO memberDTO) {
    memberService.updateMemberBySelf(memberno, memberDTO);
    
    return ResponseEntity.ok().build();
  }
  
  /**
   * 관리자가 회원을 수정한 경우
   * @param memberno 회원번호
   * @param memberDTO 변경한 값
   * @param managerno 관리자번호
   * @return 성공시 200 OK
   */
  @PutMapping(path="/update/manager/{memberno}")
  public ResponseEntity<Void> updateMemberByManager(
      @PathVariable("memberno") Long memberno,
      @RequestBody MemberDTO memberDTO,
      @RequestParam("managerno") Long managerno) {
    memberService.updateMemberByManager(memberno, memberDTO, managerno);
    
    return ResponseEntity.ok().build();
  }
  
  // 테스트 X
  /**
   * 회원 강제 탈퇴, 실제 데이터를 삭제하지 않고 상태만 정지로 변경.
   * @param memberno 탈퇴시킬 회원 번호
   * @param reason 탈퇴 사유
   * @param managerno 처리한 관리자 번호
   * @return 성공시 200 OK
   */
  @PutMapping(path="/ban/{memberno}")
  public ResponseEntity<Void> banMember(
      @PathVariable("memberno") Long memberno,
      @RequestParam("reason") String reason,
      @RequestParam("managerno") Long managerno){
    MemberDTO memberDTO = new MemberDTO();
    
    String status = "0::"+reason;
    
    memberDTO.setStatus(status);
    
    memberService.updateMemberByManager(memberno, memberDTO, managerno);
    
    return ResponseEntity.ok().build();
  }
  
 // 테스트 X
  /** 
   * 비밀번호 업데이트
   * @param memberDTO 회원정보+새비밀번호
   * @return  성공하면 true
   */
  @PostMapping(path = "/update/password")
  public ResponseEntity<Boolean> updatePassword(
      @RequestBody MemberDTO memberDTO) {
    String id = memberDTO.getId();
    String password = memberDTO.getPassword();
    
    
    boolean check = memberService.login(id, password);
    
    if(check) {
      memberService.updatePassword(id, memberDTO.getNewPassword());
    }
    
    return ResponseEntity.ok(check);
  }
}