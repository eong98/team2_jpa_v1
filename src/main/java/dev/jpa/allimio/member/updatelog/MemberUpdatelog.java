package dev.jpa.allimio.member.updatelog;

import dev.jpa.allimio.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "MEMBER_UPDATELOG")
public class MemberUpdatelog {
  /** 업데이트 로그 번호 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_updatelog_seq_use")
  @SequenceGenerator(name = "member_updatelog_seq_use", sequenceName = "MEMBER_UPDATELOG_SEQ", allocationSize = 1)
  private Long no;

  /** 회원 번호 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mno")
  private Member mno;

  /** 변경한 항목 */
  private String changedColumn;

  /** 변경 전 값 */
  private String oldValue;

  /** 변경 후 값 */
  private String newValue;

  /** 변경 일시 */
  private String changeDate;

  /** 변경자 유형 */
  private int changedBy;

  /** 변경한 관리자 번호 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updt_mnno")
  private Member updtMnno;

}
