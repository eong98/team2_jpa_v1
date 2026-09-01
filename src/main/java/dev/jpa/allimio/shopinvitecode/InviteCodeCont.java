package dev.jpa.allimio.shopinvitecode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jpa.allimio.shopinvitecode.InviteCodeDTO.Request;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invite")
public class InviteCodeCont {
  private final InviteCodeService codeService;
  
  /**
   * 코드 생성
   * @param shopno
   * @return
   */
  @PostMapping(path="/create")
  public ResponseEntity<String> createCode(
      @PathVariable("shopno") Long shopno){
    String code = codeService.createCode(shopno);
    
    return ResponseEntity.ok(code);
  }
  
  /**
   * 초대 수락 후 등록
   * @param request
   * @return
   */
  @PutMapping
  public ResponseEntity<Void> inviteMember(
      @RequestBody Request request){
    codeService.invite(request.getCode(), request.getMno());
    
    return ResponseEntity.ok().build();
  } 
}
