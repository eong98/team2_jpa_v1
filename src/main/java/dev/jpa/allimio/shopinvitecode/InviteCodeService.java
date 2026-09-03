package dev.jpa.allimio.shopinvitecode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.shop.ShopRepository;
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
  private final ShopRepository shopRepository;
  
  /** 
   * 코드 생성
   * @param shopno
   * @return
   */
  @Transactional
  public String createCode(Long shopno) {
      String now = Tool.getDate();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String expiryDate = LocalDateTime.parse(now, formatter).plusMinutes(15).format(formatter);

      // 1. 해당 매장에 기존 발급된 코드가 있는지 조회 (없으면 신규 빌드)
      InviteCode inviteCode = codeRepository.findBySno(shopno)
          .orElseGet(() -> InviteCode.builder()
              .sno(shopno) // no는 넘기지 않고 sno만 주입
              .build());

      // 2. 6자리 난수 생성 및 중복 확인
      int maxRetries = 5;
      for (int i = 0; i < maxRetries; i++) {
          String code = RandomStringUtils.insecure().next(6, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

          if (codeRepository.existsByCode(code)) {
              log.warn("초대코드 중복 발생, 재시도합니다: {}", code);
              continue;
          }

          inviteCode.setCode(code);
          inviteCode.setCdate(now);
          inviteCode.setExpiryDate(expiryDate);

          codeRepository.save(inviteCode);
          return code;
      }
    throw new RuntimeException("초대코드 생성 실패. 다시 시도해주세요.");
  }
  

  /**
   * 코드를 입력한 사용자 초대
   * @param code
   * @param memberno
   */
  @Transactional
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
    codeRepository.deleteByCode(code);
  }
  
}
