--------------------------------------------------------
-- ATTACH (첨부파일)
--------------------------------------------------------
DROP TABLE ATTACH CASCADE CONSTRAINTS;
DROP SEQUENCE ATTACH_SEQ;

CREATE TABLE ATTACH (
  NO        NUMBER(19)                  NOT NULL, -- 첨부파일번호 (PK)
  TNO       NUMBER(7)                       NULL, -- 관리자메뉴(테이블)번호 (FK) /////// 일단 작업중단
  TNAME     VARCHAR2(30)                NOT NULL, -- TNO로 가져온 테이블이름(영문) (구분되는 폴더명이 됩니다)
  BNO       NUMBER(10)                  NOT NULL, -- 등록 게시글 PK번호
  TYPE      NUMBER(10)                  NOT NULL, -- 파일 종류 구분 (0: IMAGE / 1: FILE)
  NAME      VARCHAR2(255)               NOT NULL, -- 원본 파일명
  FSIZE     NUMBER(10)   DEFAULT 0      NOT NULL, -- 파일크기 (BYTE)
  SNAME     VARCHAR2(255)               NOT NULL, -- 서버 저장 파일명
  THUMB     VARCHAR2(255)                   NULL, -- 이미지 썸네일
  PURL      VARCHAR2(500)               NOT NULL, -- 상대 저장 경로
  CDATE     VARCHAR2(30)                NOT NULL, -- 등록일시
  DDATE     VARCHAR2(30)                    NULL, -- 삭제 예정 일시 (null=정상, 값 있으면 그 시각 이후 배치가 실제 삭제)

  CONSTRAINT PK_ATTACH PRIMARY KEY (NO)
);

COMMENT ON TABLE  ATTACH       IS '첨부파일';
COMMENT ON COLUMN ATTACH.NO    IS '첨부파일번호 (PK)';
COMMENT ON COLUMN ATTACH.TNO   IS '관리자메뉴(테이블)번호 (FK)';
COMMENT ON COLUMN ATTACH.TNAME IS 'TNO로 가져온 테이블이름(영문) (구분되는 폴더명이 됩니다)';
COMMENT ON COLUMN ATTACH.BNO   IS '등록 게시글 PK번호';
COMMENT ON COLUMN ATTACH.TYPE  IS '파일 종류 구분 (0: IMAGE / 1: FILE)';
COMMENT ON COLUMN ATTACH.NAME  IS '원본 파일명';
COMMENT ON COLUMN ATTACH.FSIZE IS '파일크기 (BYTE)';
COMMENT ON COLUMN ATTACH.SNAME IS '서버 저장 파일명';
COMMENT ON COLUMN ATTACH.THUMB IS '이미지 썸네일';
COMMENT ON COLUMN ATTACH.PURL  IS '상대 저장 경로';
COMMENT ON COLUMN ATTACH.CDATE IS '등록일시';
COMMENT ON COLUMN ATTACH.DDATE IS '삭제 예정 일시 (게시글 삭제 시 즉시 삭제 대신 이 값만 세팅, 배치가 실제 삭제)';

CREATE SEQUENCE ATTACH_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 999999999999999999
  CACHE 20
  NOCYCLE;



-- SELECT *
-- FROM ATTACH A 
-- INNER JOIN IN_MENU M ON A.TNAME = M.TNAME 
-- WHERE A.BNO = 25, ;


ALTER TABLE ATTACH MODIFY (DDATE VARCHAR2(30));
COMMENT ON COLUMN ATTACH.DDATE IS '삭제 예정 일시 (게시글 삭제 시 즉시 삭제 대신 이 값만 세팅, 배치가 실제 삭제)';