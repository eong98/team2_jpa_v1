package dev.jpa.allimio.cctvstream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CctvStreamDTO {

  private long no;
  private long cno;
  private String streamUrl;
  private String protocol;
  private Integer port;
  private int connState;
  private String lastConnectedAt;
  private String cdate;

  public CctvStreamDTO() {

  }

  public CctvStreamDTO(long no, long cno, String streamUrl, String protocol, Integer port,
      int connState, String lastConnectedAt, String cdate) {
    this.no = no;
    this.cno = cno;
    this.streamUrl = streamUrl;
    this.protocol = protocol;
    this.port = port;
    this.connState = connState;
    this.lastConnectedAt = lastConnectedAt;
    this.cdate = cdate;
  }

  public CctvStream toEntity() {
    return new CctvStream(no, cno, streamUrl, protocol, port, connState, lastConnectedAt, cdate);
  }

}
