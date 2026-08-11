package dev.jpa.allimio.manager;

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

import dev.jpa.allimio.member.MemberDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dbms")
public class ManagerCont {
  private final ManagerService managerService;

//임시 DTO 생성
 public record IdCheckResponse(boolean available) {
 }

 /**
  * 아이디 중복 체크 http://10.1.205.120:9102/v1/dbms/check/{id}
  * 
  * @param id
  * @return 중복된 아이디 없음 true, 중복된 아이디 있음 false
  */
 @GetMapping(path = "/check/{id}")
 public ResponseEntity<IdCheckResponse> isIdAvailable(@PathVariable("id") String id) {
   boolean available = managerService.isIdAvailable(id);

   return ResponseEntity.ok(new IdCheckResponse(available));
 }

 /**
  * 회원가입 
  * http://localhost:9102/v1/dbms/save
  * @param managerDTO
  * @return 가입정보
  */
 @PostMapping(path = "/save")
 public ResponseEntity<Manager> save(@Valid @RequestBody ManagerDTO managerDTO) {
   Manager savedEntity = managerService.save(managerDTO);

   return ResponseEntity.ok(savedEntity);
 }

 /**
  * 비밀번호를 제외한 정보 조회
  * http://10.1.205.120:9102/v1/dbms/find
  * @return 비밀번호를 제외한 정보
  */
 @GetMapping(path = "/find")
 public ResponseEntity<List<ManagerDTO>> findAllWithoutPassword() {
   List<ManagerDTO> list = managerService.findAllWithoutPassword();

   return ResponseEntity.ok(list);
 }
 
 /**
  * 특정 관리자의 정보 조회
  * http://10.1.205.120:9102/v1/dbms/find
  * @param memberno 조회할 회원의 회원번호
  * @return 특정 회원의 정보
  */
 @GetMapping(path = "/find/{managerno}")
 public ResponseEntity<ManagerDTO> findWithNoWithoutPassword(@PathVariable(name = "managerno") Long managerno){
   ManagerDTO managerDTO = managerService.findWithNoWithoutPassword(managerno);
   
   return ResponseEntity.ok(managerDTO);
 }

 /**
  * 로그인
  * http://10.1.205.120:9102/v1/dbms/login
  * @param id
  * @param password
  * @return 성공이면 true, 실패면 false
  */
 @PostMapping(path="/login")
 @CrossOrigin(origins = "*")  // 모든 도메인으로부터 요청 허용
public ResponseEntity<Boolean> login(
    @RequestParam(name="id", defaultValue="") String id, 
    @RequestParam(name="password", defaultValue="") String password){
   boolean check = managerService.login(id, password);
   
   return ResponseEntity.ok(check);
 }

 /**
  * 관리자가 자신을 수정한 경우
  * http://localhost:9102/v1/dbms/update/self/managerno
  * @param managerno 관리자번호
  * @param managerDTO 변경한 값
  * @return 성공시 200 OK 
  */
 @PutMapping(path="/update/self/{managerno}")
 public ResponseEntity<Void> updateManagerBySelf(
     @PathVariable("managerno") Long managerno,
     @RequestBody ManagerDTO managerDTO) {
   managerService.updateManagerBySelf(managerno, managerDTO);
   
   return ResponseEntity.ok().build();
 }
 
 /**
  * 관리자가 관리자를 수정한 경우
  * http://localhost:9102/v1/dbms/update/manager/managerno
  * @param targetno 회원번호
  * @param managerDTO 변경한 값
  * @param managerno 관리자번호
  * @return 성공시 200 OK
  */
 @PutMapping(path="/update/manager/{managerno}")
 public ResponseEntity<Void> updateManagerByManager(
     @PathVariable("targetno") Long targetno,
     @RequestBody ManagerDTO managerDTO,
     @RequestParam("managerno") Long managerno) {
   managerService.updateManagerByManager(targetno, managerDTO, managerno);
   
   return ResponseEntity.ok().build();
 }
 
 //  테스트 X
 /**
  * 회원 강제 탈퇴, 실제 데이터를 삭제하지 않고 상태만 정지로 변경.
  * @param targetno 탈퇴시킬 관리자 번호
  * @param reason 탈퇴 사유
  * @param managerno 처리한 관리자 번호
  * @return 성공시 200 OK
  */
 @PutMapping(path="/ban/{managerno}")
 public ResponseEntity<Void> ban(
     @PathVariable("targetno") Long targetno,
     @RequestParam("reason") String reason,
     @RequestParam("managerno") Long managerno){
   ManagerDTO managerDTO = new ManagerDTO();
   
   String status = "0::"+reason;
   
   managerDTO.setStatus(status);
   
   managerService.updateManagerByManager(targetno, managerDTO, managerno);
   
   return ResponseEntity.ok().build();
 }
 
 // 테스트 X
 /** 
  * 비밀번호 업데이트
  * @param managerDTO 관리자정보+새비밀번호
  * @return 성공하면 true
  */
 @PostMapping(path = "/update/password")
 public ResponseEntity<Boolean> updatePassword(
     @RequestBody ManagerDTO managerDTO) {
   String id = managerDTO.getId();
   String password = managerDTO.getPassword();
   
   
   boolean check = managerService.login(id, password);
   
   if(check) {
     managerService.updatePassword(id, managerDTO.getNewPassword());
   }
   
   return ResponseEntity.ok(check);
 }
}

