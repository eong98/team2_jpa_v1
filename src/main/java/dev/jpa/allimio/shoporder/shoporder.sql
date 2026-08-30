--------------------------------------------------------
-- SHOP_ORDER (구독 내역) — 최신 통합본
--------------------------------------------------------
DROP TABLE SHOP_ORDER CASCADE CONSTRAINTS;

CREATE TABLE SHOP_ORDER (
  NO                 VARCHAR2(30)                NOT NULL, -- 구독 내역 랜덤번호 (PK)
  MNO                NUMBER(10)                  NOT NULL, -- 회원 번호 (FK -> MEMBER.NO)
  PNO                NUMBER(7)                   NOT NULL, -- 구독권(상품) 번호 (FK -> SHOP_PLAN.NO)
  SNO                NUMBER(7)                       NULL, -- 매장 번호 (FK -> SHOP.NO), 결제 후 매장 등록시 저장
  PMONTH             NUMBER(2)                   NOT NULL, -- 선택 기간 (6, 12)
  CCNT               NUMBER(2)                   NOT NULL, -- 구독시 지정한 CCTV 개수
  BPRICE             NUMBER(12,2)                NOT NULL, -- CCTV 1대당 기본단가 (결제 시점 스냅샷)
  TOTALPRICE         NUMBER(12)                  NOT NULL, -- 총 결제 금액 (원)
  STATUS             NUMBER(1)       DEFAULT 0   NOT NULL, -- 구독 상태 (0: 대기, 1: 정상, 2: 만료, 3: 취소)
  SDATE              VARCHAR2(30)                    NULL, -- 구독 시작일 (매장 연결 확정 시점에 채워짐)
  EDATE              VARCHAR2(30)                    NULL, -- 구독 종료일 (매장 연결 확정 시점에 채워짐)
  CDATE              VARCHAR2(30)                NOT NULL, -- 구매일시
  UDATE              VARCHAR2(30)                    NULL, -- 구매 변경일시 (환불, 취소, 갱신, 변경신청/확정)

  -- 구독권 변경(대수/등급/기간) 승인 대기용 컬럼. CCTV 대수 변경은 매장에 실제
  -- 설치가 완료돼야 확정되므로, 신청 시점엔 기존 값(PNO/PMONTH/CCNT/BPRICE/
  -- TOTALPRICE/EDATE)을 그대로 두고 아래 PENDING_* 컬럼에만 신청값을 저장합니다.
  -- 관리자 승인 시 PENDING_* → 실제 컬럼으로 확정 반영 후 초기화됩니다.
  -- (STATUS=0이면서 PENDING_CCNT가 NULL이 아니면 "변경 승인 대기",
  --  PENDING_CCNT가 NULL이면 "매장 연결 대기"로 구분)
  PENDING_PNO        NUMBER(7)                       NULL, -- 변경 신청된 구독권 번호 (등급이 바뀌는 경우)
  PENDING_PMONTH     NUMBER(2)                       NULL, -- 변경 신청된 이용기간 (6, 12)
  PENDING_CCNT       NUMBER(2)                       NULL, -- 변경 신청된 CCTV 대수
  PENDING_BPRICE     NUMBER(12,2)                    NULL, -- 변경 신청된 등급의 대당단가 (스냅샷)
  PENDING_TOTALPRICE NUMBER(12)                      NULL, -- 변경 확정 시 반영될 총 결제금액
  PENDING_EDATE      VARCHAR2(30)                    NULL, -- 변경 확정 시 반영될 새 종료일

  CONSTRAINT PK_SHOP_ORDER PRIMARY KEY (NO),
  CONSTRAINT FK_SHOP_ORDER_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO),
  CONSTRAINT FK_SHOP_ORDER_PNO FOREIGN KEY (PNO) REFERENCES SHOP_PLAN (NO),
  CONSTRAINT FK_SHOP_ORDER_SNO FOREIGN KEY (SNO) REFERENCES SHOP (NO)
);

COMMENT ON TABLE  SHOP_ORDER                    IS '회원 매장별 구독 내역';
COMMENT ON COLUMN SHOP_ORDER.NO                 IS '구독 내역 랜덤번호 (PK)';
COMMENT ON COLUMN SHOP_ORDER.MNO                IS '회원 번호 (FK -> MEMBER.NO)';
COMMENT ON COLUMN SHOP_ORDER.PNO                IS '구독권(상품) 번호 (FK -> SHOP_PLAN.NO)';
COMMENT ON COLUMN SHOP_ORDER.SNO                IS '매장 번호 (FK -> SHOP.NO), 결제 후 매장 등록시 저장';
COMMENT ON COLUMN SHOP_ORDER.PMONTH             IS '선택 기간 (6, 12)';
COMMENT ON COLUMN SHOP_ORDER.CCNT               IS '구독시 지정한 CCTV 개수';
COMMENT ON COLUMN SHOP_ORDER.BPRICE             IS 'CCTV 1대당 기본단가 (결제 시점 스냅샷)';
COMMENT ON COLUMN SHOP_ORDER.TOTALPRICE         IS '총 결제 금액 (원)';
COMMENT ON COLUMN SHOP_ORDER.STATUS             IS '구독 상태 (0: 대기, 1: 정상, 2: 만료, 3: 취소)';
COMMENT ON COLUMN SHOP_ORDER.SDATE              IS '구독 시작일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.EDATE              IS '구독 종료일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.CDATE              IS '구매일시 (YYYY-MM-DD HH24:MI:SS)';
COMMENT ON COLUMN SHOP_ORDER.UDATE              IS '구매 변경일시 (환불, 취소, 갱신, 변경신청/확정)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_PNO        IS '변경 신청된 구독권 번호 (등급 변경 시)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_PMONTH     IS '변경 신청된 이용기간 (6, 12)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_CCNT       IS '변경 신청된 CCTV 대수';
COMMENT ON COLUMN SHOP_ORDER.PENDING_BPRICE     IS '변경 신청된 등급의 대당단가 (스냅샷)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_TOTALPRICE IS '변경 확정 시 반영될 총 결제금액';
COMMENT ON COLUMN SHOP_ORDER.PENDING_EDATE      IS '변경 확정 시 반영될 새 종료일';

-- 활성(STATUS=1: 정상) 구독은 매장당 1건만 허용 — 실수로 중복 연결하는 것 자체를
-- DB 레벨에서 막기 위한 안전장치
CREATE UNIQUE INDEX UX_SHOP_ORDER_ACTIVE_SNO
  ON SHOP_ORDER (CASE WHEN STATUS = 1 THEN SNO END);
  



  ALTER TABLE SHOP_ORDER MODIFY (SDATE NULL, EDATE NULL);
  
  
ALTER TABLE SHOP_ORDER 
RENAME CONSTRAINT NO TO PK_SHOP_ORDER;

SELECT CONSTRAINT_NAME 
FROM USER_CONSTRAINTS 
WHERE TABLE_NAME = 'SHOP_ORDER' 
  AND CONSTRAINT_TYPE = 'P';

ALTER TABLE SHOP_REFUND 
RENAME COLUMN PAYMENTNO TO PNO;


--------------------------------------------------------
-- SHOP_ORDER: 구독권 변경(대수/등급/기간) 대기용 컬럼 추가
--------------------------------------------------------
ALTER TABLE SHOP_ORDER ADD (
  PENDING_PNO         NUMBER(7)      NULL, -- 변경 신청된 구독권 번호 (등급이 바뀌는 경우)
  PENDING_PMONTH      NUMBER(2)      NULL, -- 변경 신청된 이용기간 (6, 12)
  PENDING_CCNT        NUMBER(2)      NULL, -- 변경 신청된 CCTV 대수
  PENDING_BPRICE      NUMBER(12,2)   NULL, -- 변경 신청된 등급의 대당단가 (스냅샷)
  PENDING_TOTALPRICE  NUMBER(12)     NULL, -- 변경 확정 시 반영될 총 결제금액
  PENDING_EDATE       VARCHAR2(30)   NULL  -- 변경 확정 시 반영될 새 종료일
);

COMMENT ON COLUMN SHOP_ORDER.PENDING_PNO        IS '변경 신청된 구독권 번호 (등급 변경 시)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_PMONTH     IS '변경 신청된 이용기간 (6, 12)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_CCNT       IS '변경 신청된 CCTV 대수';
COMMENT ON COLUMN SHOP_ORDER.PENDING_BPRICE     IS '변경 신청된 등급의 대당단가 (스냅샷)';
COMMENT ON COLUMN SHOP_ORDER.PENDING_TOTALPRICE IS '변경 확정 시 반영될 총 결제금액';
COMMENT ON COLUMN SHOP_ORDER.PENDING_EDATE      IS '변경 확정 시 반영될 새 종료일';

COMMENT ON COLUMN SHOP_ORDER.STATUS IS
  '구독 상태 (0: 대기 - 매장미연결 또는 PENDING_CCNT 존재시 변경승인대기, 1: 정상, 2: 만료, 3: 취소)';

DROP INDEX UX_SHOP_ORDER_ACTIVE_SNO;
CREATE UNIQUE INDEX UX_SHOP_ORDER_ACTIVE_SNO
  ON SHOP_ORDER (CASE WHEN STATUS = 1 THEN SNO END);