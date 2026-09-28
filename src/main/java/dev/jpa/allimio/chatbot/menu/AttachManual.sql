--------------------------------------------------------
-- ATTACH_MANUAL (챗봇상담 - 옵션형 메뉴 자동생성용 첨부문서 목록)
--------------------------------------------------------
-- 관리자가 [AI 옵션생성]에 쓸 md 문서를 업로드해두는 순수 첨부파일 목록.
-- 업로드된 문서가 "어느 카테고리를 만드는 데 쓰였는지"는 이 테이블이
-- 추적하지 않는다 — 문서 하나에 여러 주제가 섞여 있을 수 있어 1:1 매핑이
-- 애초에 부정확하고, [AI 옵션생성]을 누를 때마다 그 시점에 업로드되어
-- 있는 문서 전체를 다시 종합해서 CHAT_MENU 트리를 새로 만들면 충분하기
-- 때문이다. 생성 결과(카테고리/하위메뉴/답변)는 전부 CHAT_MENU에 저장된다.

DROP TABLE ATTACH_MANUAL CASCADE CONSTRAINTS;
DROP SEQUENCE ATTACH_MANUAL_SEQ;

CREATE TABLE ATTACH_MANUAL (
  NO          NUMBER(10)    NOT NULL, -- 문서 고유번호(PK)
  FILENAME    VARCHAR2(200) NOT NULL, -- 원본 파일명 (관리자가 업로드한 그대로, 화면 표시용)
  UPLOAD_PATH VARCHAR2(500) NOT NULL, -- 서버에 저장된 실제 파일 경로 (FastAPI가 읽을 때 사용)
  CDATE       VARCHAR2(30)  NOT NULL, -- 업로드일시
  UPDATEYN    CHAR(1)       DEFAULT 'N' NOT NULL, -- 마지막 AI생성 반영여부 (N이 하나라도 있으면 AI옵션생성 버튼 활성화)

  CONSTRAINT PK_ATTACH_MANUAL PRIMARY KEY (NO)
);

COMMENT ON TABLE  ATTACH_MANUAL             IS '챗봇 옵션형 메뉴 자동생성용 첨부문서 목록 (순수 파일목록, 분류정보 없음)';
COMMENT ON COLUMN ATTACH_MANUAL.NO          IS '문서 고유번호(PK)';
COMMENT ON COLUMN ATTACH_MANUAL.FILENAME    IS '원본 파일명';
COMMENT ON COLUMN ATTACH_MANUAL.UPLOAD_PATH IS '서버 저장 실제 경로';
COMMENT ON COLUMN ATTACH_MANUAL.CDATE       IS '업로드일시';
COMMENT ON COLUMN ATTACH_MANUAL.UPDATEYN IS '마지막 AI생성 반영여부 (N이 하나라도 있으면 AI옵션생성 버튼 활성화)';

CREATE SEQUENCE ATTACH_MANUAL_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999999999999
  NOCACHE
  NOCYCLE;