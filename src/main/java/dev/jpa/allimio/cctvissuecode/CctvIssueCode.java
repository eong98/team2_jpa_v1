package dev.jpa.allimio.cctvissuecode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/* ---------------------------------------------------------------------
   이상행동유형코드(CCTV_ISSUE_CODE) 참조 테이블.

   CODE는 시퀀스로 채번하지 않는 자연키(business key)입니다 - CCTV_ISSUE.CODE와
   똑같은 값(예: "01")을 그대로 PK로 씁니다. AI가 이미 이 코드값으로 이슈를 저장하고
   있어서(cctv_issue 파이프라인), 새 대리키(surrogate key)를 만들면 오히려 더 헷갈립니다.
--------------------------------------------------------------------- */
@Entity
@Getter
@Setter
@ToString
public class CctvIssueCode {

  @Id
  private String code;

  private String codeName;
  private String description;
  private int severity;
  private int ord;

  @Column(name = "USE_YN")
  private String useYn;

  private String cdate;

  public CctvIssueCode() {

  }

  public CctvIssueCode(String code, String codeName, String description, int severity, int ord,
      String useYn, String cdate) {
    this.code = code;
    this.codeName = codeName;
    this.description = description;
    this.severity = severity;
    this.ord = ord;
    this.useYn = useYn;
    this.cdate = cdate;
  }

}
