-- 문의 메일 발송 이력 (답변 알림 + 재설정 인증번호)
-- 문의글을 영구 삭제해도 기록은 남김 → QA에 FK를 걸지 않음
CREATE TABLE QA_SEND_LOG (
  NO       NUMBER(10)      NOT NULL,
  QNO      NUMBER(10)      NOT NULL,          -- 문의글 번호 (QA.NO, FK 없음)
  MNO      NUMBER(10)      NULL,              -- 받는 회원 번호 (비회원 글이면 NULL)
  EMAIL    VARCHAR2(100)   NOT NULL,          -- 받는 사람 이메일
  STYPE    NUMBER(1)       NOT NULL,          -- 1: 답변 등록 알림, 2: 비밀번호 재설정 인증번호
  TITLE    VARCHAR2(300)   NOT NULL,          -- 메일 제목
  CONTENT  VARCHAR2(2000)  NULL,              -- 메일 본문 (인증번호 메일은 저장하지 않음)
  RESULT   CHAR(1)         NOT NULL,          -- Y: 발송 성공, N: 실패
  MSG      VARCHAR2(500)   NULL,              -- 실패 사유
  CDATE    VARCHAR2(30)    NOT NULL,          -- 발송 일시
  CONSTRAINT PK_QA_SEND_LOG PRIMARY KEY (NO)
);

CREATE SEQUENCE QA_SEND_LOG_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX IDX_QA_SEND_LOG_QNO ON QA_SEND_LOG (QNO);

COMMENT ON TABLE  QA_SEND_LOG         IS '문의사항 메일 발송 이력';
COMMENT ON COLUMN QA_SEND_LOG.QNO     IS '문의글 번호 (삭제된 글의 기록도 남기기 위해 FK 없음)';
COMMENT ON COLUMN QA_SEND_LOG.MNO     IS '받는 회원 번호 (비회원 글이면 NULL)';
COMMENT ON COLUMN QA_SEND_LOG.STYPE   IS '1: 답변 등록 알림, 2: 비밀번호 재설정 인증번호';
COMMENT ON COLUMN QA_SEND_LOG.TITLE   IS '메일 제목';
COMMENT ON COLUMN QA_SEND_LOG.CONTENT IS '메일 본문 (인증번호 메일은 저장하지 않음)';


ALTER TABLE QA MODIFY (PW NULL);
commit;