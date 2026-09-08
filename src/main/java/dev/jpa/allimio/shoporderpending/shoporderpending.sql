--------------------------------------------------------
-- SHOP_ORDER_PENDING (구독 변경신청)
--------------------------------------------------------
DROP TABLE SHOP_ORDER_PENDING CASCADE CONSTRAINTS;
DROP SEQUENCE SHOP_ORDER_PENDING_LOG_SEQ;

CREATE TABLE SHOP_ORDER_PENDING (
  NO                 NUMBER(7)                   NOT NULL, -- 구독 변경 신청번호 (PK)
  ONO                VARCHAR2(30)                NOT NULL, -- 구독 내역 랜덤번호 (FK)
  MNO                NUMBER(10)                  NOT NULL, -- 주문한 회원번호
  PNO                NUMBER(7)                       NULL, -- 변경 신청된 구독권 상품 번호 (등급 변경 시) (FK -> SHOP_PLAN.NO)
  CCNT               NUMBER(2)                       NULL, -- 변경 신청된 CCTV 대수
  BPRICE             NUMBER(12,2)                    NULL, -- 변경 신청된 등급의 CCTV 대당단가 (스냅샷)
  PMONTH             NUMBER(2)                       NULL, -- 변경 신청된 이용기간 (6, 12)
  EDATE              VARCHAR2(30)                    NULL, -- 변경 확정시 반영될 새 종료일
  TOTALPRICE         NUMBER(12)                      NULL, -- 변경 확정시 반영될 총 결제금액
  STATUS             NUMBER(1)       DEFAULT 0   NOT NULL, -- 변경신청 상태(0: 승인대기, 1:승인반려, 2:완료)
  MEMO               CLOB                            NULL, -- 승인 반려 사유 (관리자 입력)
  CDATE              VARCHAR2(30)                NOT NULL, -- 변경 신청일시
  UDATE              VARCHAR2(30)                    NULL, -- 변경 완료일시

  CONSTRAINT PK_SHOP_ORDER_PENDING PRIMARY KEY (NO),
  CONSTRAINT FK_SHOP_ORDER_PENDING_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO),
  CONSTRAINT FK_SHOP_ORDER_PENDING_PNO FOREIGN KEY (PNO) REFERENCES SHOP_PLAN (NO),
  CONSTRAINT FK_SHOP_ORDER_PENDING_ONO FOREIGN KEY (ONO) REFERENCES SHOP_ORDER (NO)
);

COMMENT ON TABLE  SHOP_ORDER_PENDING                    IS '회원 매장별 구독 변경신청';
COMMENT ON COLUMN SHOP_ORDER_PENDING.NO                 IS '구독 변경 신청번호 (PK)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.ONO                IS '구독 내역 랜덤번호 (FK)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.MNO                IS '주문한 회원번호';
COMMENT ON COLUMN SHOP_ORDER_PENDING.PNO                IS '변경 신청된 구독권 상품 번호 (등급 변경 시) (FK -> SHOP_PLAN.NO)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.CCNT               IS '변경 신청된 CCTV 대수';
COMMENT ON COLUMN SHOP_ORDER_PENDING.BPRICE             IS '변경 신청된 등급의 CCTV 대당단가 (스냅샷)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.PMONTH             IS '변경 신청된 이용기간 (6, 12)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.EDATE              IS '변경 확정시 반영될 새 종료일';
COMMENT ON COLUMN SHOP_ORDER_PENDING.TOTALPRICE         IS '변경 확정시 반영될 총 결제금액';
COMMENT ON COLUMN SHOP_ORDER_PENDING.STATUS             IS '변경신청 상태(0: 승인대기, 1:승인반려, 2:완료)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.MEMO            IS '승인 반려 사유 (관리자 입력)';
COMMENT ON COLUMN SHOP_ORDER_PENDING.CDATE              IS '변경 신청일시';
COMMENT ON COLUMN SHOP_ORDER_PENDING.UDATE              IS '변경 완료일시';


CREATE SEQUENCE SHOP_ORDER_PENDING_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9999997
    NOCACHE
    NOCYCLE;


--------------------------------------------------------
-- SHOP_ORDER.PENDING_* → SHOP_ORDER_PENDING 데이터 이관
-- (컬럼 삭제 전에 반드시 먼저 실행)
--------------------------------------------------------

INSERT INTO SHOP_ORDER_PENDING (
  NO, ONO, MNO, PNO, CCNT, BPRICE, PMONTH, EDATE, TOTALPRICE, STATUS, CDATE
)
SELECT
  SHOP_ORDER_PENDING_LOG_SEQ.NEXTVAL,
  so.NO,
  so.MNO,
  so.PENDING_PNO,
  so.PENDING_CCNT,
  so.PENDING_BPRICE,
  so.PENDING_PMONTH,
  so.PENDING_EDATE,
  so.PENDING_TOTALPRICE,
  0,                              -- 승인대기 상태로 이관
  NVL(so.UDATE, so.CDATE)         -- 신청일시 대체값 (정확한 신청일 컬럼이 원본에 없음)
FROM SHOP_ORDER so
WHERE so.PENDING_CCNT IS NOT NULL;

COMMIT;

-- 이관 전 대상 건수
SELECT COUNT(*) FROM SHOP_ORDER WHERE PENDING_CCNT IS NOT NULL;

-- 이관 후 건수 (일치해야 함)
SELECT COUNT(*) FROM SHOP_ORDER_PENDING;

-- 내용 대조 (샘플 확인)
SELECT so.NO, so.PENDING_CCNT, sop.CCNT
FROM SHOP_ORDER so
JOIN SHOP_ORDER_PENDING sop ON sop.ONO = so.NO
WHERE so.PENDING_CCNT IS NOT NULL;