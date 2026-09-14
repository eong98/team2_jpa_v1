
--------------------------------------------------------
-- CHAT_MENU (챗봇상담 - 옵션형 선택지 트리)
--------------------------------------------------------

DROP TABLE CHAT_MENU CASCADE CONSTRAINTS;
DROP SEQUENCE CHAT_MENU_SEQ;

CREATE TABLE CHAT_MENU (
  NO          NUMBER(10)                  NOT NULL, -- 메뉴 고유번호(PK)
  PNO         NUMBER(10)                      NULL, -- 상위 메뉴 번호 (FK, 최상위면 NULL)
  STEP        NUMBER(1)                   NOT NULL, -- 옵션 단계 (1~3)
  LABEL       VARCHAR2(100)               NOT NULL, -- 선택지에 표시될 텍스트
  ANSWER      CLOB                            NULL, -- 이 선택지 클릭 시 노출할 답변(최상위 STEP1은 보통 NULL)
  USERAG      CHAR(1)         DEFAULT 'N' NOT NULL, -- 이 노드 도달 시 RAG 검색도 같이 수행할지 (Y/N)
  VSEQ        NUMBER(3)       DEFAULT 0   NOT NULL, -- 같은 부모 안에서의 노출 순서
  USEYN       CHAR(1)         DEFAULT 'Y' NOT NULL, -- 사용 여부 (N이면 목록에서 숨김)
  CDATE       VARCHAR2(30)                NOT NULL, -- 등록일시

  CONSTRAINT PK_CHAT_MENU PRIMARY KEY (NO),
  CONSTRAINT FK_CHAT_MENU_PNO FOREIGN KEY (PNO) REFERENCES CHAT_MENU (NO)
);

COMMENT ON TABLE  CHAT_MENU        IS '챗봇옵션 메뉴 (옵션형 선택지 트리, 최대 3단계)';
COMMENT ON COLUMN CHAT_MENU.NO     IS '메뉴 고유번호(PK)';
COMMENT ON COLUMN CHAT_MENU.PNO    IS '상위 메뉴 번호 (FK, 최상위면 NULL)';
COMMENT ON COLUMN CHAT_MENU.STEP   IS '옵션 단계 (1~3)';
COMMENT ON COLUMN CHAT_MENU.LABEL  IS '선택지에 표시될 텍스트';
COMMENT ON COLUMN CHAT_MENU.ANSWER IS '이 선택지 클릭 시 노출할 답변(최상위 STEP1은 보통 NULL)';
COMMENT ON COLUMN CHAT_MENU.USERAG IS 'RAG검색여부 - 이 노드 도달 시 RAG 검색도 같이 수행할지 (Y/N)';
COMMENT ON COLUMN CHAT_MENU.VSEQ   IS '노출순서 - 같은 부모 안에서의 노출 순서';
COMMENT ON COLUMN CHAT_MENU.USEYN  IS '사용여부 (N이면 목록에서 숨김)';
COMMENT ON COLUMN CHAT_MENU.CDATE  IS '등록일시';


CREATE SEQUENCE CHAT_MENU_SEQ 
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999999999999
  NOCACHE
  NOCYCLE;

CREATE INDEX IX_CHAT_MENU_PNO ON CHAT_MENU (PNO);

-----------------------------------------------------------------
--------------------------------------------------------
-- CHAT_MENU 초기 데이터 (구독권 상담 트리, UI/UX 테스트용)
--------------------------------------------------------

-- ══════════════════════════════════════════════
-- STEP1 — 최상위 선택지
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL, NULL, 1, '구독권',
        '구독권에 관한 문의에 맞는 옵션을 선택해주세요.',
        'N', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL, NULL, 1, '회원가입',
        '서비스 준비 중 입니다.',
        'N', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL, NULL, 1, 'CCTV',
        '서비스 준비 중 입니다.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL, NULL, 1, '알림',
        '서비스 준비 중 입니다.',
        'N', 4, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

COMMIT;

-- ══════════════════════════════════════════════
-- STEP2 — "구독권"(부모) 하위
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL, 
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권' AND PNO IS NULL), 
        2, '구독권 요금제 안내',
        '구독권은 CCTV 대수에 따라 베이직/프로/엔터프라이즈 3종류가 있으며, 6개월/12개월 중 선택하실 수 있습니다.',
        'N', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL, 
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권' AND PNO IS NULL), 
        2, '구독권 결제/매장 연결',
        '구독권 결제 후, 원하시는 매장에 연결하시면 이용이 시작됩니다.',
        'N', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,  
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권' AND PNO IS NULL), 
        2, '구독권 갱신',
        '구독 종료일 7일 전부터 갱신이 가능합니다.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,  
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권' AND PNO IS NULL), 
         2, '구독권 변경 (기간/대수)',
        '이용 중인 구독권의 기간이나 CCTV 대수를 변경하실 수 있습니다.',
        'N', 4, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,  
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권' AND PNO IS NULL), 
         2, '구독권 취소/환불',
        '구독 취소 시 남은 기간에 대해 환불이 진행됩니다.',
        'N', 5, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

COMMIT;

-- ══════════════════════════════════════════════
-- STEP3 — "구독권 요금제 안내"(부모) 하위
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 요금제 안내'),
        3, '등급별 대수/가격이 궁금해요',
        '베이직(1~3대) 6개월 32,000원/12개월 27,000원, 프로(4~6대) 6개월 27,000원/12개월 22,000원, 엔터프라이즈(7~10대) 6개월 22,000원/12개월 18,000원(대당 월 단가)입니다.',
        'Y', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 요금제 안내'),
        3, '6개월과 12개월 중 뭐가 유리해요?',
        '12개월 약정이 대당 단가가 더 저렴합니다. 장기 이용 예정이시면 12개월을 추천드립니다.',
        'N', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 요금제 안내'),
        3, '11대 이상도 가능한가요?',
        '현재는 최대 10대까지 지원됩니다. 그 이상 필요하시면 고객센터로 문의해주세요.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));


-- ══════════════════════════════════════════════
-- STEP3 — "구독권 결제/매장 연결"(부모) 하위
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 결제/매장 연결'),
        3, '결제는 어떻게 하나요',
        '구독권 페이지에서 등급/대수/기간을 선택 후 카드, 계좌이체, 토스페이 중 원하는 수단으로 결제하실 수 있습니다.',
        'N', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 결제/매장 연결'),
        3, '결제 후 매장 연결이 안 돼요',
        '결제하신 CCTV 대수와 매장에 실제 등록된 CCTV 대수가 일치해야 연결이 가능합니다. 매장의 CCTV 등록 현황을 확인해주세요.',
        'N', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 결제/매장 연결'),
        3, '결제만 하고 매장은 나중에 연결해도 되나요',
        '네, 결제 후 매장 미연결 상태(대기)로 유지되며, 원하실 때 매장을 선택해 연결하실 수 있습니다.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));


-- ══════════════════════════════════════════════
-- STEP3 — "구독권 갱신"(부모) 하위
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 갱신'),
        3, '갱신은 언제부터 가능한가요',
        '구독 만료 7일 전부터 만료 시점까지 갱신하실 수 있습니다. 만료 후에는 갱신이 불가하니 기간 내 갱신해주세요.',
        'N', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 갱신'),
        3, '갱신하면 대수도 바꿀 수 있나요',
        '갱신은 기간 연장 전용입니다. CCTV 대수를 변경하고 싶으시면 ''구독권 변경'' 메뉴를 이용해주세요.',
        'N', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 갱신'),
        3, '이미 만료됐는데 갱신할 수 있나요',
        '죄송하지만 만료된 구독은 갱신이 불가합니다. 새로 구독권을 결제해주시기 바랍니다.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));


-- ══════════════════════════════════════════════
-- STEP3 — "구독권 변경 (기간/대수)"(부모) 하위
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 변경 (기간/대수)'),
        3, '기간을 변경하고 싶어요',
        '6개월에서 12개월로 늘리는 것은 언제든 가능합니다. 12개월에서 6개월로 줄이는 것은 남은 기간이 275일 이상일 때만 가능합니다.',
        'N', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 변경 (기간/대수)'),
        3, 'CCTV 대수를 변경하고 싶어요',
        '대수 변경은 구독 종료까지 28일 이상 남아있어야 신청 가능하며, 관리자가 실제 매장의 CCTV 설치 상태를 확인한 후 승인됩니다.',
        'Y', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 변경 (기간/대수)'),
        3, '변경 신청하면 바로 적용되나요',
        '기간 변경은 즉시 반영됩니다. 대수 변경은 관리자 승인이 필요하며, 승인 전까지는 기존 조건으로 계속 이용하실 수 있습니다.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));


-- ══════════════════════════════════════════════
-- STEP3 — "구독권 취소/환불"(부모) 하위
-- ══════════════════════════════════════════════

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 취소/환불'),
        3, '취소는 언제까지 가능한가요',
        '구독 종료일까지 28일 이상 남아있어야 취소가 가능합니다. 단, 매장 연결 전이라면 기간 제한 없이 취소하실 수 있습니다.',
        'N', 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 취소/환불'),
        3, '환불 금액은 어떻게 계산되나요',
        '결제하신 단가 기준으로, 사용한 기간을 제외한 나머지 기간만큼 일할 계산되어 환불됩니다.',
        'Y', 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO CHAT_MENU (NO, PNO, STEP, LABEL, ANSWER, USERAG, VSEQ, USEYN, CDATE)
VALUES (CHAT_MENU_SEQ.NEXTVAL,
        (SELECT NO FROM CHAT_MENU WHERE LABEL = '구독권 취소/환불'),
        3, '환불은 얼마나 걸리나요',
        '환불계좌 등록 후 영업일 기준 3~5일 내 처리됩니다. 처리 현황은 마이페이지에서 확인하실 수 있습니다.',
        'N', 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));

COMMIT;