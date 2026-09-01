package dev.jpa.allimio.shopinvitecode;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.shopmember.ShopMemberDTO;
import dev.jpa.allimio.shopmember.ShopMemberRepository;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InviteCodeService {
  private final InviteCodeRepository codeRepository;
  private final ShopMemberRepository shopMemberRepository;
  
  /** 
   * 코드 생성
   * @param shopno
   * @return
   */
  public String createCode(Long shopno) {
    
    String now = Tool.getDate();
    
    int maxRetries = 5;
    
    for (int i = 0; i < maxRetries; i++) {
        try {
          String code = RandomStringUtils.insecure()
              .next(6, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

            InviteCode inviteCode = InviteCode.builder()
                .sno(shopno)                // 매장 번호 주입
                .code(code)     // 생성된 무작위 코드 주입
                .cdate(now)
                .expiryDate(now)
                .build();

            codeRepository.save(inviteCode); // Unique 제약조건 위반 시 예외 발생
            return code; // 저장 성공 시 리턴 후 종료
        } catch (DataIntegrityViolationException e) {
            // 중복이 발생하면 캐치하여 다음 루프(재시도)로 넘어감
            log.warn("초대코드 중복 발생, 재시도합니다.");
        }
    }
    throw new RuntimeException("초대코드 생성 실패. 다시 시도해주세요.");
  }
  
  /**
   * 코드를 입력한 사용자 초대
   * @param code
   * @param memberno
   */
  public void invite(String code, Long memberno) {
    String now = Tool.getDate();
    
    InviteCode invitecode = codeRepository.findByCode(code)
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 존재하지 않는 초대코드입니다: " + code));
    
    if(now.compareTo(invitecode.getExpiryDate()) > 0) {
      throw new IllegalStateException("이미 만료된 초대코드입니다.");
    }
      
    ShopMemberDTO invite = ShopMemberDTO.builder()
        .sno(invitecode.getSno())
        .mno(memberno)
        .cdate(now)
        .build();
      
    shopMemberRepository.save(invite.toEntity());
  }
  
}
