CREATE TABLE SHOP_ORDER_LOG (
  NO         NUMBER(10)                  NOT NULL, -- 로그 고유번호 (PK)
  ORDERNO    VARCHAR2(30)                NOT NULL, -- 구독 내역 번호 (FK -> SHOP_ORDER.ORDERNO)
  MNO        NUMBER(10)                  NOT NULL, -- 회원번호 (조회 편의용 비정규화)
  ACTION     NUMBER(1)                   NOT NULL, -- 이벤트 종류 (0: 결제, 1: 매장연결, 2: 갱신, 3: 취소)
  SNO        NUMBER(7)                       NULL, -- 관련 매장번호 (매장연결일 때만 값 있음)
  BEFORE_EDATE VARCHAR2(30)                  NULL, -- 변경 전 종료일 (갱신일 때)
  AFTER_EDATE  VARCHAR2(30)                  NULL, -- 변경 후 종료일 (결제/매장연결/갱신일 때)
  AMOUNT     NUMBER(12)                      NULL, -- 관련 금액 (결제: 결제액, 취소: 환불액)
  MEMO       VARCHAR2(500)                   NULL, -- 부가 설명 (예: "사용 3개월 · 환불 3개월")
  CDATE      VARCHAR2(30)                NOT NULL, -- 발생일시

  CONSTRAINT PK_SHOP_ORDER_LOG PRIMARY KEY (NO),
  CONSTRAINT FK_SHOP_ORDER_LOG_ORDERNO FOREIGN KEY (ORDERNO) REFERENCES SHOP_ORDER (ORDERNO)
);

COMMENT ON TABLE  SHOP_ORDER_LOG              IS '구독 내역 변경 이력 (결제·매장연결·갱신·취소 로그)';
COMMENT ON COLUMN SHOP_ORDER_LOG.ORDERNO      IS '구독 내역 번호 (FK -> SHOP_ORDER.ORDERNO)';
COMMENT ON COLUMN SHOP_ORDER_LOG.MNO          IS '회원번호 (조회 편의용 비정규화)';
COMMENT ON COLUMN SHOP_ORDER_LOG.ACTION       IS '이벤트 종류 (0: 결제, 1: 매장연결, 2: 갱신, 3: 취소)';
COMMENT ON COLUMN SHOP_ORDER_LOG.SNO          IS '관련 매장번호 (매장연결일 때만 값 있음)';
COMMENT ON COLUMN SHOP_ORDER_LOG.BEFORE_EDATE IS '변경 전 종료일 (갱신일 때)';
COMMENT ON COLUMN SHOP_ORDER_LOG.AFTER_EDATE  IS '변경 후 종료일 (결제/매장연결/갱신일 때)';
COMMENT ON COLUMN SHOP_ORDER_LOG.AMOUNT       IS '관련 금액 (결제: 결제액, 취소: 환불액)';
COMMENT ON COLUMN SHOP_ORDER_LOG.MEMO         IS '부가 설명';
COMMENT ON COLUMN SHOP_ORDER_LOG.CDATE        IS '발생일시';

CREATE SEQUENCE SHOP_ORDER_LOG_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9999997
    NOCACHE
    NOCYCLE;

CREATE INDEX IX_SHOP_ORDER_LOG_ORDERNO ON SHOP_ORDER_LOG (ORDERNO);
CREATE INDEX IX_SHOP_ORDER_LOG_MNO ON SHOP_ORDER_LOG (MNO);