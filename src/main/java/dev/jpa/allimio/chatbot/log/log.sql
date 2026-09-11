
--------------------------------------------------------
-- CHAT_LOG (챗봇대화로그)
--------------------------------------------------------
DROP TABLE CHAT_LOG CASCADE CONSTRAINTS;
DROP SEQUENCE CHAT_LOG_SEQ;

CREATE TABLE CHAT_LOG (
  NO       NUMBER(10)                  NOT NULL, -- 대화 로그 번호 (PK)
  SNO      VARCHAR2(36)                NOT NULL, -- 현재 세션 식별키 (UUID, FK) - 상담 1건 단위
  SENDER   NUMBER(1)                   NOT NULL, -- 발송자 유형 (0: 사용자, 1: AI, 2: 시스템)
  MTYPE    NUMBER(1)                   NOT NULL, -- 메시지 유형 (0: 선택지선택, 1: 답변노출, 2: 자유텍스트, 3: 뒤로가기, 4: AI답변, 5: 시스템안내)
  CONTENT  CLOB                        NOT NULL, -- 화면에 보일 메시지 내용 (CLOB)
  CNO      NUMBER(10)                      NULL, -- 선택지 관련 메시지면 그 메뉴 노드 번호 (FK -> CHAT_MENU.NO)
  CDATE    VARCHAR2(30)                NOT NULL, -- 로그 작성일시

  CONSTRAINT PK_CHAT_LOG PRIMARY KEY (NO),
  CONSTRAINT FK_CHAT_LOG_SNO FOREIGN KEY (SNO) REFERENCES CHAT_SESSION (NO),
  CONSTRAINT FK_CHAT_LOG_CNO FOREIGN KEY (CNO) REFERENCES CHAT_MENU (NO)
);

COMMENT ON TABLE  CHAT_LOG          IS '챗봇대화로그 (세션 안의 메시지 시간순 기록)';
COMMENT ON COLUMN CHAT_LOG.NO       IS '대화 로그 번호 (PK)';
COMMENT ON COLUMN CHAT_LOG.SNO      IS '현재 세션 식별키 (UUID, FK) - 상담 1건 단위';
COMMENT ON COLUMN CHAT_LOG.SENDER   IS '발송자 유형 (0: 사용자, 1: AI, 2: 시스템)';
COMMENT ON COLUMN CHAT_LOG.MTYPE    IS '메시지 유형 (0: 선택지선택, 1: 답변노출, 2: 자유텍스트, 3: 뒤로가기, 4: AI답변, 5: 시스템안내)';
COMMENT ON COLUMN CHAT_LOG.CONTENT  IS '화면에 보일 메시지 내용 (CLOB)';
COMMENT ON COLUMN CHAT_LOG.CNO      IS '선택지 관련 메시지면 그 메뉴 노드 번호 (FK -> CHAT_MENU.NO)';
COMMENT ON COLUMN CHAT_LOG.CDATE    IS '로그 작성일시';

CREATE SEQUENCE CHAT_LOG_SEQ 
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999999999999
  NOCACHE
  NOCYCLE;

CREATE INDEX IX_CHAT_LOG_SNO ON CHAT_LOG (SNO);