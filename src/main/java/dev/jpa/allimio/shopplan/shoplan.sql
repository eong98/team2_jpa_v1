
--------------------------------------------------------
-- SHOP_PLAN (구독권)
--------------------------------------------------------
DROP TABLE SHOP_PLAN CASCADE CONSTRAINTS;
DROP SEQUENCE SHOP_PLAN_SEQ;

CREATE TABLE SHOP_PLAN (
  NO          NUMBER(7)                   NOT NULL, -- 구독권번호 (PK)
  PNAME       VARCHAR2(100)               NOT NULL, -- 구독권명
  PMONTH      NUMBER(2)                   NOT NULL, -- 이용기간 (6, 12)
  MINCCTV     NUMBER(3)                   NOT NULL, -- 이용기간별 CCTV 최소 갯수 지정
  MAXCCTV     NUMBER(3)                   NOT NULL, -- 이용기간별 CCTV 최대 갯수 지정
  BPRICE      NUMBER(12, 2)               NOT NULL, -- CCTV 1대당 기본단가(개월수)
  DESCRIPTION VARCHAR2(500)                   NULL, -- 구독권설명
  ISSELL      CHAR(1)         DEFAULT 'Y' NOT NULL, -- 판매여부 (Y/N)
  CDATE       VARCHAR2(30)                NOT NULL, -- 등록일

  CONSTRAINT PK_SHOP_PLAN PRIMARY KEY (NO)
);

COMMENT ON TABLE  SHOP_PLAN             IS '구독권 상품 정보';
COMMENT ON COLUMN SHOP_PLAN.NO          IS '구독권 고유 번호 (PK)';
COMMENT ON COLUMN SHOP_PLAN.PNAME       IS '구독권 명칭';
COMMENT ON COLUMN SHOP_PLAN.PMONTH      IS '이용 기간 (6: 6개월, 12: 12개월)';
COMMENT ON COLUMN SHOP_PLAN.MINCCTV     IS '이용기간별 CCTV 최소 갯수 지정';
COMMENT ON COLUMN SHOP_PLAN.MAXCCTV     IS '이용기간별 CCTV 최대 갯수 지정';
COMMENT ON COLUMN SHOP_PLAN.BPRICE      IS 'CCTV 1대당 기본 단가';
COMMENT ON COLUMN SHOP_PLAN.DESCRIPTION IS '구독권 상세 설명';
COMMENT ON COLUMN SHOP_PLAN.ISSELL      IS '판매여부 (Y/N)';
COMMENT ON COLUMN SHOP_PLAN.CDATE       IS '구독권 등록일';

CREATE SEQUENCE SHOP_PLAN_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9999997
    NOCACHE
    NOCYCLE;
