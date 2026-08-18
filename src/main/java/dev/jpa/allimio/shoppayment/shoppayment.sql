
--------------------------------------------------------
-- SHOP_PAYMENT (결제내역)
--------------------------------------------------------
DROP TABLE SHOP_PAYMENT CASCADE CONSTRAINTS;
DROP SEQUENCE SHOP_PAYMENT_SEQ;

CREATE TABLE SHOP_PAYMENT (
    NO          NUMBER(10)                  NOT NULL, -- 결제내역 고유번호 PK
    ONO         NUMBER(10)                  NOT NULL, -- 구독내역 번호 (FK -> SHOP_ORDER.NO)
    MNO         NUMBER(10)                  NOT NULL, -- 회원번호 (FK -> SHOP_ORDER.MNO)
    PRICE       NUMBER(12)                  NOT NULL, -- 결제금액 (원)
    PMETHOD     NUMBER(2)                   NOT NULL, -- 결제수단 (0: 카드, 1:계좌이체, 2: 토스페이)
    PSTATUS     NUMBER(1)       DEFAULT 0   NOT NULL, -- 결제 상태 (0: 결제완료, 1: 결제실패, 2: 결제취소)
    CDATE       VARCHAR2(30)                NOT NULL, -- 결제일시
    UDATE       VARCHAR2(30)                    NULL, -- 결제 상태 변경일시 (환불일, 취소일)

    CONSTRAINT PK_SHOP_PAYMENT PRIMARY KEY (NO),
    CONSTRAINT FK_SHOP_PAYMENT_ONO FOREIGN KEY (ONO) REFERENCES SHOP_ORDER (NO),
    CONSTRAINT FK_SHOP_PAYMENT_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO)
);

COMMENT ON TABLE  SHOP_PAYMENT          IS '구독 결제 내역';
COMMENT ON COLUMN SHOP_PAYMENT.NO        IS '결제 내역 고유 번호 (PK)';
COMMENT ON COLUMN SHOP_PAYMENT.ONO       IS '구독 내역 번호 (FK -> SHOP_ORDER.NO)';
COMMENT ON COLUMN SHOP_PAYMENT.MNO       IS '회원 번호 (FK -> MEMBER.NO)';
COMMENT ON COLUMN SHOP_PAYMENT.PRICE     IS '실 결제 금액 (원)';
COMMENT ON COLUMN SHOP_PAYMENT.PMETHOD   IS '결제 수단 (0: 카드, 1: 계좌이체, 2: 토스페이)';
COMMENT ON COLUMN SHOP_PAYMENT.PSTATUS   IS '결제 상태 (0: 결제완료, 1: 결제실패, 2: 결제취소)';
COMMENT ON COLUMN SHOP_PAYMENT.CDATE     IS '결제일시 (YYYY-MM-DD HH24:MI:SS)';
COMMENT ON COLUMN SHOP_PAYMENT.UDATE     IS '결제 상태 변경일시 (환불일, 취소일)';

CREATE SEQUENCE SHOP_PAYMENT_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9999997
    NOCACHE
    NOCYCLE;