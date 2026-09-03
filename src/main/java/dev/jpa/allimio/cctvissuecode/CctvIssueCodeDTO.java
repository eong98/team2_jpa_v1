package dev.jpa.allimio.cctvissuecode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CctvIssueCodeDTO {

  private String code;
  private String codeName;
  private String description;
  private int severity;
  private int ord;
  private String useYn;
  private String cdate;

  public CctvIssueCodeDTO() {

  }

  public CctvIssueCodeDTO(String code, String codeName, String description, int severity, int ord,
      String useYn, String cdate) {
    this.code = code;
    this.codeName = codeName;
    this.description = description;
    this.severity = severity;
    this.ord = ord;
    this.useYn = useYn;
    this.cdate = cdate;
  }

  public CctvIssueCode toEntity() {
    return new CctvIssueCode(code, codeName, description, severity, ord, useYn, cdate);
  }

}
