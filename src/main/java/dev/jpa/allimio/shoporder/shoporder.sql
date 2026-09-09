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
  STATUS             NUMBER(1)       DEFAULT 0   NOT NULL, -- 구독 상태 (0: 대기, 1: 정상, 2: 취소)
  SDATE              VARCHAR2(30)                    NULL, -- 구독 시작일 (매장 연결 확정 시점에 채워짐)
  EDATE              VARCHAR2(30)                    NULL, -- 구독 종료일 (매장 연결 확정 시점에 채워짐)
  CDATE              VARCHAR2(30)                NOT NULL, -- 구매일시
  UDATE              VARCHAR2(30)                    NULL, -- 구매 변경일시 (환불, 취소, 갱신, 변경신청/확정)

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
COMMENT ON COLUMN SHOP_ORDER.STATUS             IS '구독 상태 (0: 대기, 1: 정상, 2: 취소)';
COMMENT ON COLUMN SHOP_ORDER.SDATE              IS '구독 시작일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.EDATE              IS '구독 종료일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.CDATE              IS '구매일시 (YYYY-MM-DD HH24:MI:SS)';
COMMENT ON COLUMN SHOP_ORDER.UDATE              IS '구매 변경일시 (환불, 취소, 갱신, 변경신청/확정)';

  



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
-- SHOP_ORDER: 구독권 변경(대수/등급/기간) 대기용 컬럼/코멘트/인덱스 원상복구
-- (SHOP_ORDER_PENDING 별도 테이블로 분리했으므로 정리)
--------------------------------------------------------

-- 1. PENDING_* 컬럼 전체 삭제 (컬럼 삭제 시 해당 컬럼 코멘트는 자동 삭제됨)
ALTER TABLE SHOP_ORDER DROP (
  PENDING_PNO,
  PENDING_PMONTH,
  PENDING_CCNT,
  PENDING_BPRICE,
  PENDING_TOTALPRICE,
  PENDING_EDATE
);

-- 2. STATUS 코멘트를 원래 4단계 체계로 복구
COMMENT ON COLUMN SHOP_ORDER.STATUS IS '구독 상태 (0: 대기, 1: 정상, 2: 취소)';

-- 3. 인덱스는 STATUS=1(정상) 기준 그대로라 원래도 PENDING_*과 무관했음 —
--    다만 원본 스크립트에 DROP+CREATE가 세트로 있었으니 동일하게 재생성
DROP INDEX UX_SHOP_ORDER_ACTIVE_SNO;
CREATE UNIQUE INDEX UX_SHOP_ORDER_ACTIVE_SNO
  ON SHOP_ORDER (CASE WHEN STATUS = 1 THEN SNO END);



SELECT so.*, sp.pname
FROM SHOP_ORDER so
LEFT JOIN SHOP_PLAN sp ON so.pno = sp.no
WHERE so.mno = :mno
  AND (:word IS NULL OR sp.pname LIKE '%' || :word || '%')
ORDER BY so.status ASC, so.cdate DESC;