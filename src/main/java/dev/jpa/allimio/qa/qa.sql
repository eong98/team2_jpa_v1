--------------------------------------------------------
-- QA (1:1 문의게시판)
--------------------------------------------------------
DROP TABLE QA CASCADE CONSTRAINTS;
DROP SEQUENCE QA_SEQ;

CREATE TABLE QA(
  NO          NUMBER(10)                  NOT NULL, -- 문의사항 번호
  MNO         NUMBER(5,0)                   NOT NULL, -- 문의하는 회원번호(FK)
  TYPE        NUMBER(7)                   NOT NULL, -- 0: 기타, 1: 관제신청, 2: 영상요청, 3: 장비장애
  TITLE       VARCHAR2(200)               NOT NULL, -- 문의제목
  CONTENT     CLOB                        NOT NULL, -- 문의내용
  CDATE       VARCHAR2(30)                NOT NULL, -- 등록일
  PW          VARCHAR2(300)               NOT NULL, -- 문의글 비밀번호
  STATUS      NUMBER(1)   DEFAULT 0       NOT NULL, -- 문의답변 상태 (0: 답변대기, 1: 확인중, 2:답변완료)
  VMODE       CHAR(1)     DEFAULT 'N'     NOT NULL, -- 비밀글 여부(Y/N)
  VSEQ        NUMBER(5)                       NULL, -- 자주묻는 질문용 정렬순서
  ANO         NUMBER(10)                       NULL, -- 답변자 고유번호
  ANSWER      CLOB                            NULL, -- 답변 내용
  ADATE       VARCHAR2(30)                    NULL, -- 답변 등록일
  ISDEL       CHAR(1)     DEFAULT 'N'         NULL, -- 삭제여부(Y/N)
  DDATE       VARCHAR2(30)                    NULL, -- 삭제일시

  CONSTRAINT PK_QA PRIMARY KEY (NO),
--  CONSTRAINT FK_QA_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO)
--  CONSTRAINT FK_QA_MNO FOREIGN KEY (ANO) REFERENCES MANAGER (NO)
  CONSTRAINT FK_QA_MNO FOREIGN KEY (MNO) REFERENCES ACCOUNT (ACCOUNTNO)
);

COMMENT ON TABLE  QA         IS 'Q&A 문의사항';
COMMENT ON COLUMN QA.NO      IS '문의사항 고유 번호 (PK)';
COMMENT ON COLUMN QA.MNO     IS '문의 회원 번호 (FK -> MEMBER.NO)';
COMMENT ON COLUMN QA.TYPE    IS '문의 유형 (0: 기타, 1: 관제신청, 2: 영상요청, 3: 장비장애)';
COMMENT ON COLUMN QA.TITLE   IS '문의 제목';
COMMENT ON COLUMN QA.CONTENT IS '문의 내용';
COMMENT ON COLUMN QA.CDATE   IS '등록 일시';
COMMENT ON COLUMN QA.PW      IS '문의글 비밀번호';
COMMENT ON COLUMN QA.STATUS  IS '답변 상태 (0: 답변대기, 1: 확인중, 2: 답변완료)';
COMMENT ON COLUMN QA.VMODE   IS '비밀글 여부 (Y/N)';
COMMENT ON COLUMN QA.VSEQ    IS '자주묻는 질문(FAQ) 정렬 순서';
COMMENT ON COLUMN QA.ANO     IS '답변자(관리자) 고유 번호';
COMMENT ON COLUMN QA.ANSWER  IS '답변 내용';
COMMENT ON COLUMN QA.ADATE   IS '답변 등록 일시';
COMMENT ON COLUMN QA.ISDEL   IS '삭제 여부 (Y/N)';
COMMENT ON COLUMN QA.DDATE   IS '삭제 일시';

CREATE SEQUENCE QA_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 20
  NOCYCLE;
