
--------------------------------------------------------
-- NOTICE (공지사항)
--------------------------------------------------------
DROP TABLE NOTICE CASCADE CONSTRAINTS;
DROP SEQUENCE NOTICE_SEQ;

CREATE TABLE NOTICE (
  NO       NUMBER(10)                  NOT NULL, -- 공지사항번호 (PK)
  ANO      NUMBER(10)                  NOT NULL, -- 공지사항 작성 회원번호(FK)
  TYPE     NUMBER(1)                   NOT NULL, -- 공지유형 (0: 긴급, 1: 중요, 2: 알림, 3: 신규)
  TITLE    VARCHAR2(200)               NOT NULL, -- 제목
  CONTENT  CLOB                        NOT NULL, -- 내용
  PW       VARCHAR2(300)               NOT NULL, -- 게시글 비밀번호
  CDATE    VARCHAR2(30)                NOT NULL, -- 등록일
  VCNT     NUMBER(10,0) DEFAULT 0          NULL, -- 조회수
  FIXYN    CHAR(1)      DEFAULT 'N'    NOT NULL, -- 상단 고정여부 (Y/N)
  FILEYN   CHAR(1)      DEFAULT 'N'        NULL, -- 첨부파일 존재여부 (Y/N)
  VMODE    CHAR(1)      DEFAULT 'Y'    NOT NULL, -- 출력모드 (Y/N)
  VSEQ     NUMBER(5)                   NOT NULL, -- 출력순서
  ISDEL    CHAR(1)      DEFAULT 'N'        NULL, -- 삭제여부(Y/N)
  DDATE    VARCHAR2(30)                    NULL, -- 삭제일시

  CONSTRAINT PK_NOTICE PRIMARY KEY (NO),
  CONSTRAINT FK_NOTICE_ANO FOREIGN KEY (ANO) REFERENCES MANAGER (NO)
);

COMMENT ON TABLE  NOTICE         IS '공지사항';
COMMENT ON COLUMN NOTICE.NO      IS '공지사항번호 (PK)';
COMMENT ON COLUMN NOTICE.ANO     IS '공지사항 작성 회원 번호 (FK -> MANAGER.NO)';
COMMENT ON COLUMN NOTICE.TYPE    IS '공지유형 (0: 긴급, 1: 중요, 2: 알림, 3: 신규)';
COMMENT ON COLUMN NOTICE.TITLE   IS '제목';
COMMENT ON COLUMN NOTICE.CONTENT IS '내용';
COMMENT ON COLUMN NOTICE.PW      IS '게시글 비밀번호';
COMMENT ON COLUMN NOTICE.CDATE   IS '등록일';
COMMENT ON COLUMN NOTICE.VCNT    IS '조회수';
COMMENT ON COLUMN NOTICE.FIXYN   IS '상단 고정여부 (Y/N)';
COMMENT ON COLUMN NOTICE.FILEYN   IS '첨부파일 존재여부 (Y/N)';
COMMENT ON COLUMN NOTICE.VMODE   IS '출력모드 (공개/비공개: Y/N)';
COMMENT ON COLUMN NOTICE.VSEQ    IS '정렬순서';
COMMENT ON COLUMN NOTICE.ISDEL    IS '삭제여부 (Y/N)';
COMMENT ON COLUMN NOTICE.DDATE    IS '삭제일시';

CREATE SEQUENCE NOTICE_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 20
  NOCYCLE;
