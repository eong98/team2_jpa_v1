
--------------------------------------------------------
-- SHOP_ORDER (회원 매장별 구독 내역)
--------------------------------------------------------
DROP TABLE SHOP_ORDER CASCADE CONSTRAINTS;
DROP SEQUENCE SHOP_ORDER_SEQ;

CREATE TABLE SHOP_ORDER (
  NO          VARCHAR2(30)                NOT NULL, -- 구독 내역 랜덤번호 (PK)
  MNO         NUMBER(10)                  NOT NULL, -- 회원번호 (FK)
  PNO         NUMBER(7)                   NOT NULL, -- 구독권(상품) 번호 (FK -> SHOP_PLAN.NO)
  SNO         NUMBER(7)                       NULL, -- 매장 번호 (FK -> SHOP.NO), 결제 후 매장 등록시 저장
  PMONTH      NUMBER(2)                   NOT NULL, -- 선택 기간 (6: 6개월, 12: 12개월)
  CCNT        NUMBER(2)                   NOT NULL, -- 구독시 지정한 CCTV 개수
  BPRICE      NUMBER(12, 2)               NOT NULL, -- CCTV 1대당 기본단가 (결제 시점 스냅샷, SHOP_PLAN.BPRICE 변경과 무관)
  TOTALPRICE  NUMBER(12)                  NOT NULL, -- 총 결제 금액 (원)
  STATUS      NUMBER(1)       DEFAULT 0   NOT NULL, -- 구독 상태 (0: 정상, 1: 만료됨, 2: 취소)
  SDATE       VARCHAR2(30)                    NULL, -- 구독 시작일 (YYYY-MM-DD)
  EDATE       VARCHAR2(30)                    NULL, -- 구독 종료일 (YYYY-MM-DD)
  CDATE       VARCHAR2(30)                NOT NULL, -- 구매일시 (YYYY-MM-DD HH24:MI:SS)
  UDATE       VARCHAR2(30)                    NULL, -- 구매 변경일시 (환불, 취소, 갱신)

  CONSTRAINT PK_SHOP_ORDER PRIMARY KEY (NO),
  CONSTRAINT FK_SHOP_ORDER_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO),
  CONSTRAINT FK_SHOP_ORDER_PNO FOREIGN KEY (PNO) REFERENCES SHOP_PLAN (NO),
  CONSTRAINT FK_SHOP_ORDER_SNO FOREIGN KEY (SNO) REFERENCES SHOP (NO)
);

COMMENT ON TABLE  SHOP_ORDER            IS '회원 매장별 구독 내역';
COMMENT ON COLUMN SHOP_ORDER.NO         IS '구독 내역 랜덤번호 (PK)';
COMMENT ON COLUMN SHOP_ORDER.MNO        IS '회원 번호 (FK -> MEMBER.NO)';
COMMENT ON COLUMN SHOP_ORDER.PNO        IS '구독권(상품) 번호 (FK -> SHOP_PLAN.NO)';
COMMENT ON COLUMN SHOP_ORDER.SNO        IS '매장 번호 (FK -> SHOP.NO), 결제 후 매장 등록시 저장';
COMMENT ON COLUMN SHOP_ORDER.PMONTH     IS '선택 기간 (6: 6개월, 12: 12개월)';
COMMENT ON COLUMN SHOP_ORDER.CCNT       IS '구독시 지정한 CCTV 개수';
COMMENT ON COLUMN SHOP_ORDER.BPRICE     IS 'CCTV 1대당 기본단가 (결제 시점 스냅샷)';
COMMENT ON COLUMN SHOP_ORDER.TOTALPRICE IS '총 결제 금액 (원)';
COMMENT ON COLUMN SHOP_ORDER.STATUS     IS '구독 상태 (0: 정상, 1: 만료됨, 2: 취소)';
COMMENT ON COLUMN SHOP_ORDER.SDATE      IS '구독 시작일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.EDATE      IS '구독 종료일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.CDATE      IS '구매일시 (YYYY-MM-DD HH24:MI:SS)';
COMMENT ON COLUMN SHOP_ORDER.UDATE      IS '구매 변경일시 (환불, 취소, 갱신)';

-- 활성(STATUS=0) 구독은 매장당 1건만 허용 — 점주 1명 구조라 동시성 이슈는 없지만,
-- 실수로 중복 연결하는 것 자체를 DB 레벨에서 막기 위한 안전장치
CREATE UNIQUE INDEX UX_SHOP_ORDER_ACTIVE_SNO
  ON SHOP_ORDER (CASE WHEN STATUS = 0 THEN SNO END);
  



  ALTER TABLE SHOP_ORDER MODIFY (SDATE NULL, EDATE NULL);
  
  
ALTER TABLE SHOP_ORDER 
RENAME CONSTRAINT NO TO PK_SHOP_ORDER;

SELECT CONSTRAINT_NAME 
FROM USER_CONSTRAINTS 
WHERE TABLE_NAME = 'SHOP_ORDER' 
  AND CONSTRAINT_TYPE = 'P';

ALTER TABLE SHOP_REFUND 
RENAME COLUMN PAYMENTNO TO PNO;