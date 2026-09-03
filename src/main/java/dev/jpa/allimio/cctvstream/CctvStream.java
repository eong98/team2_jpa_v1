package dev.jpa.allimio.cctvstream;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/* ---------------------------------------------------------------------
   CCTV 스트림 연결정보(CCTV_STREAM). CCTV(장비 메타데이터)와 1:1(cno UNIQUE) -
   CCTV 테이블 자체에는 스트림 주소 컬럼이 없어서(cctv-ai-pipeline-design.md
   "확인/보완이 필요한 것" 1번) 별도 테이블로 분리했습니다. Jetson 워커가 이 표를
   참고해 카메라에 접속하고, 접속/재접속 성공 시 CONN_STATE·LAST_CONNECTED_AT을 갱신합니다.
--------------------------------------------------------------------- */
@Entity
@Getter
@Setter
@ToString
public class CctvStream {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cctv_stream_seq_use")
  @SequenceGenerator(name = "cctv_stream_seq_use", sequenceName = "SEQ_CCTV_STREAM_NO", allocationSize = 1)
  private long no;

  private long cno;
  private String streamUrl;
  private String protocol;
  private Integer port;
  private int connState;
  private String lastConnectedAt;
  private String cdate;

  public CctvStream() {

  }

  public CctvStream(long no, long cno, String streamUrl, String protocol, Integer port,
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

}
