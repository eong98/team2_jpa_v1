
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
  VSEQ        NUMBER(3)       DEFAULT 0   NOT NULL, -- 같은 부모 안에서의 노출 순서
  USEYN       CHAR(1)         DEFAULT 'Y' NOT NULL, -- 사용 여부 (N이면 목록에서 숨김, 옵션형메뉴 자동생성시 검토전 'N')
  CDATE       VARCHAR2(30)                NOT NULL, -- 등록일시
  AIYN        CHAR(1)         DEFAULT 'N' NOT NULL, -- AI관리여부 (Y: AI가 생성, N: 관리자가 직접 등록)
  ANO         NUMBER(10)                      NULL, -- AI생성 원본 문서번호 (ATTACH_MANUAL.NO, 관리자 등록 메뉴는 NULL)
 
  CONSTRAINT PK_CHAT_MENU PRIMARY KEY (NO),
  CONSTRAINT FK_CHAT_MENU_PNO FOREIGN KEY (PNO) REFERENCES CHAT_MENU (NO)
);
 
COMMENT ON TABLE  CHAT_MENU        IS '챗봇옵션 메뉴 (옵션형 선택지 트리, 최대 3단계)';
COMMENT ON COLUMN CHAT_MENU.NO     IS '메뉴 고유번호(PK)';
COMMENT ON COLUMN CHAT_MENU.PNO    IS '상위 메뉴 번호 (FK, 최상위면 NULL)';
COMMENT ON COLUMN CHAT_MENU.STEP   IS '옵션 단계 (1~3)';
COMMENT ON COLUMN CHAT_MENU.LABEL  IS '선택지에 표시될 텍스트';
COMMENT ON COLUMN CHAT_MENU.ANSWER IS '이 선택지 클릭 시 노출할 답변(최상위 STEP1은 보통 NULL)';
COMMENT ON COLUMN CHAT_MENU.VSEQ   IS '노출순서 - 같은 부모 안에서의 노출 순서';
COMMENT ON COLUMN CHAT_MENU.USEYN  IS '사용여부 (N이면 목록에서 숨김)';
COMMENT ON COLUMN CHAT_MENU.CDATE  IS '등록일시';
COMMENT ON COLUMN CHAT_MENU.AIYN   IS 'AI관리여부 - AI가 생성한 노드면 Y, 관리자가 등록한 노드면 N (재생성 시 Y만 삭제)';
COMMENT ON COLUMN CHAT_MENU.ANO    IS 'AI생성 원본 문서번호(ATTACH_MANUAL.NO) - 그 문서 수정/삭제 시 이 번호의 AI메뉴만 교체/삭제';
 
 
CREATE SEQUENCE CHAT_MENU_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999999999999
  NOCACHE
  NOCYCLE;
 
CREATE INDEX IX_CHAT_MENU_PNO ON CHAT_MENU (PNO);
CREATE INDEX IX_CHAT_MENU_ANO ON CHAT_MENU (ANO);

