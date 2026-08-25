package dev.jpa.allimio.member.token;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.history.update.UpdateHistoryDTO;
import dev.jpa.allimio.history.update.UpdateHistoryRepository;
import dev.jpa.allimio.member.Member;
import dev.jpa.allimio.member.MemberRepository;
import dev.jpa.allimio.tool.MailService;
import dev.jpa.allimio.tool.Tool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
  
  private final UpdateHistoryRepository updateHistoryRepository;
  private final MemberRepository memberRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;
  
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  /** 토큰 생성 */
  @Transactional
  public String createResetToken(String id, String email) {
    Member member = memberRepository.findByIdAndEmail(id, email)
        .orElseThrow(() -> new IllegalArgumentException("아이디 또는 이메일 정보가 일치하지 않습니다"));
    
    // 토큰 생성
    String token = UUID.randomUUID().toString();
    String now = Tool.getDate();
    
    
    String expiryDate = LocalDateTime.now().plusMinutes(30).format(formatter);
    
 // 토큰 저장 또는 갱신
    PasswordResetToken resetToken = tokenRepository.findById(member.getNo())
        .map(existing -> {
            existing.setToken(token);
            existing.setExpiryDate(expiryDate);
            existing.setCdate(now);
            
            return existing;
        })
        .orElseGet(() -> PasswordResetToken.builder()
            .member(member)
            .token(token)
            .expiryDate(expiryDate)
            .cdate(now)
            .build());

    tokenRepository.save(resetToken);
    
    mailService.sendPasswordResetMail(member.getEmail(), token);
    
    return token;
  }
  
  /** 토큰 유효성 검사 */
  public PasswordResetToken validateToken(String token) {
    PasswordResetToken resetToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 재설정 링크입니다."));


   LocalDateTime expiry = LocalDateTime.parse(resetToken.getExpiryDate(), formatter);

   if (expiry.isBefore(LocalDateTime.now())) {
     throw new IllegalArgumentException("만료된 링크입니다. 재설정 요청을 다시 진행해 주세요.");
   }

   return resetToken;
  }
  
  /** 패스워드 변경 */
  public void resetPassword(String token, String newPassword) {
    // 토큰 검증
    PasswordResetToken resetToken = validateToken(token);

    // 토큰에 연결된 회원 조회
    Member member = resetToken.getMember();

    // PasswordEncoder를 사용하여 새 비밀번호 암호화 후 변경
    String encodedPassword = passwordEncoder.encode(newPassword);
    member.setPassword(encodedPassword); // 프로젝트 엔티티의 비밀번호 세터/메서드명에 맞게 조정
    
    saveUpdateLogs(member);

    // 사용이 완료된 토큰 삭제 (1회성 사용 보장)
    tokenRepository.delete(resetToken);
  }
  
  /** 비밀번호 변경 로그 */
  private void saveUpdateLogs(Member member) {
    String now = Tool.getDate();
    
    UpdateHistoryDTO log = UpdateHistoryDTO.builder()
        .mno(member.getNo())
        .mnno(null)
        .changedColumn("password")
        .oldValue("changed_password")
        .newValue("chagend_passwrd")
        .changeDate(now)
        .changedBy(0)
        .updtMnno(null)
        .build();
    
    updateHistoryRepository.save(log.toEntity());
  }

}
