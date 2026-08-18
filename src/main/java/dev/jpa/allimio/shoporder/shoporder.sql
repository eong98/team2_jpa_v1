
--------------------------------------------------------
-- SHOP_ORDER (회원 매장별 구독 내역)
--------------------------------------------------------
DROP TABLE SHOP_ORDER CASCADE CONSTRAINTS;
DROP SEQUENCE SHOP_ORDER_SEQ;

CREATE TABLE SHOP_ORDER (
    NO          NUMBER(10)                  NOT NULL, -- 구독내역 번호 (PK)
    PNO         NUMBER(7)                   NOT NULL, -- 구독권(상품) 번호 (FK)
    MNO         NUMBER(10)                  NOT NULL, -- 회원번호 (FK)
    SNO         NUMBER(7)                       NULL, -- 매장번호 (FK), 결제 후 매장 등록시 저장
    PMONTH      NUMBER(2)                   NOT NULL, -- 선택기간 (6, 12)
    CCNT        NUMBER(2)                   NOT NULL, -- CCTV 갯수 (1-10)
    TOTALPRICE  NUMBER(12)                  NOT NULL, -- 결제 금액(원)
    STATUS      NUMBER(1)       DEFAULT 0   NOT NULL, -- 구독상태 (0: 정상, 1: 만료됨, 2: 취소)
    SDATE       VARCHAR2(30)                NOT NULL, -- 구독 시작일
    EDATE       VARCHAR2(30)                NOT NULL, -- 구독 종료일
    CDATE       VARCHAR2(30)                NOT NULL, -- 등록일
    UDATE       VARCHAR2(30)                    NULL, -- 구매변경일

    CONSTRAINT PK_SHOP_ORDER PRIMARY KEY (NO),
    CONSTRAINT FK_SHOP_ORDER_PNO FOREIGN KEY (PNO) REFERENCES SHOP_PLAN (NO),
    CONSTRAINT FK_SHOP_ORDER_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO),
    CONSTRAINT FK_SHOP_ORDER_SNO FOREIGN KEY (SNO) REFERENCES SHOP (NO)
);


COMMENT ON TABLE  SHOP_ORDER            IS '회원 매장별 구독 내역';
COMMENT ON COLUMN SHOP_ORDER.NO         IS '구독 내역 고유 번호 (PK)';
COMMENT ON COLUMN SHOP_ORDER.PNO        IS '구독권(상품) 번호 (FK -> SHOP_PLAN.NO)';
COMMENT ON COLUMN SHOP_ORDER.MNO        IS '회원 번호 (FK -> MEMBER.NO)';
COMMENT ON COLUMN SHOP_ORDER.SNO        IS '매장 번호 (FK -> SHOP.NO), 결제 후 매장 등록시 저장';
COMMENT ON COLUMN SHOP_ORDER.PMONTH     IS '선택 기간 (6: 6개월, 12: 12개월)';
COMMENT ON COLUMN SHOP_ORDER.CCNT       IS '연동 CCTV 개수 (1~10대)';
COMMENT ON COLUMN SHOP_ORDER.TOTALPRICE IS '총 결제 금액 (원)';
COMMENT ON COLUMN SHOP_ORDER.STATUS     IS '구독 상태 (0: 정상, 1: 만료됨, 2: 취소)';
COMMENT ON COLUMN SHOP_ORDER.SDATE      IS '구독 시작일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.EDATE      IS '구독 종료일 (YYYY-MM-DD)';
COMMENT ON COLUMN SHOP_ORDER.CDATE       IS '구매일시 (YYYY-MM-DD HH24:MI:SS)';
COMMENT ON COLUMN SHOP_ORDER.UDATE       IS '구매 변경일시 (환불, 취소, 재구매)';

CREATE SEQUENCE SHOP_ORDER_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9999997
    NOCACHE
    NOCYCLE;
