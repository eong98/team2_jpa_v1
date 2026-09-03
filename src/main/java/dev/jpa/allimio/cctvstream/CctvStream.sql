--------------------------------------------------------
-- CCTV_STREAM (CCTV 스트림 연결정보)
--------------------------------------------------------
DROP TABLE CCTV_STREAM CASCADE CONSTRAINTS;
DROP SEQUENCE SEQ_CCTV_STREAM_NO;

CREATE TABLE CCTV_STREAM (
  NO                NUMBER(9)                   NOT NULL, -- 스트림 고유번호 (PK)
  CNO               NUMBER(9)                   NOT NULL, -- CCTV번호 (FK -> CCTV.NO), CCTV 1대당 1건(UNIQUE)
  STREAM_URL        VARCHAR2(300)               NOT NULL, -- 스트림 접속 주소 (예: rtsp://192.168.0.10:554/stream1)
  PROTOCOL          VARCHAR2(10)    DEFAULT 'RTSP' NOT NULL, -- 스트림 프로토콜 (RTSP/RTMP/HTTP/HLS 등)
  PORT              NUMBER(5)                       NULL, -- 접속 포트
  CONN_STATE        NUMBER(1)       DEFAULT 0   NOT NULL, -- 연결상태 (0: 미연결, 1: 연결됨, 2: 오류)
  LAST_CONNECTED_AT VARCHAR2(19)                    NULL, -- 최근 연결(또는 재연결) 성공 일시
  CDATE             VARCHAR2(19)                NOT NULL, -- 등록일시

  CONSTRAINT PK_CCTV_STREAM PRIMARY KEY (NO),
  CONSTRAINT UQ_CCTV_STREAM_CNO UNIQUE (CNO),
  CONSTRAINT FK_CCTV_STREAM_CNO FOREIGN KEY (CNO) REFERENCES CCTV (NO) ON DELETE CASCADE
);

COMMENT ON TABLE  CCTV_STREAM                    IS 'CCTV 스트림 연결정보 (Jetson 워커가 실시간으로 붙는 영상 스트림 주소/연결상태)';
COMMENT ON COLUMN CCTV_STREAM.NO                 IS '스트림 고유 번호 (PK)';
COMMENT ON COLUMN CCTV_STREAM.CNO                IS 'CCTV 번호 (FK -> CCTV.NO), CCTV 1대당 스트림 1건(1:1)';
COMMENT ON COLUMN CCTV_STREAM.STREAM_URL         IS '스트림 접속 주소 (rtsp://, rtmp://, http:// 등)';
COMMENT ON COLUMN CCTV_STREAM.PROTOCOL           IS '스트림 프로토콜 (RTSP/RTMP/HTTP/HLS 등)';
COMMENT ON COLUMN CCTV_STREAM.PORT               IS '접속 포트';
COMMENT ON COLUMN CCTV_STREAM.CONN_STATE         IS '연결상태 (0: 미연결, 1: 연결됨, 2: 오류)';
COMMENT ON COLUMN CCTV_STREAM.LAST_CONNECTED_AT  IS '최근 연결(재연결) 성공 일시';
COMMENT ON COLUMN CCTV_STREAM.CDATE              IS '등록일시';

CREATE SEQUENCE SEQ_CCTV_STREAM_NO
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 참고: CNO에 UNIQUE 제약을 걸어뒀기 때문에 Oracle이 자동으로 유니크 인덱스를 만들어줍니다
-- (CctvStreamRepository.findByCno 조회 성능 별도 인덱스 불필요).
