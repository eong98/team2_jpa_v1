--------------------------------------------------------
-- QA (1:1 문의게시판)
--------------------------------------------------------
DROP TABLE QA CASCADE CONSTRAINTS;
DROP SEQUENCE QA_SEQ;

CREATE TABLE QA(
  NO          NUMBER(10)                   NOT NULL, -- 문의사항 번호
  MNO         NUMBER(10)                   NOT NULL, -- 문의하는 회원번호(FK)
  TYPE        NUMBER(7)                    NOT NULL, -- 0: 기타, 1: 관제신청, 2: 영상요청, 3: 장비장애
  TITLE       VARCHAR2(200)                NOT NULL, -- 문의제목
  CONTENT     CLOB                         NOT NULL, -- 문의내용
  CDATE       VARCHAR2(30)                 NOT NULL, -- 등록일
  PW          VARCHAR2(300)                NOT NULL, -- 문의글 비밀번호
  STATUS      NUMBER(1)    DEFAULT 0       NOT NULL, -- 문의답변 상태 (0: 답변대기, 1: 확인중, 2:답변완료)
  VMODE       CHAR(1)      DEFAULT 'N'     NOT NULL, -- 비밀글 여부(Y/N)
  VSEQ        NUMBER(5)                        NULL, -- 자주묻는 질문용 정렬순서
  ANO         NUMBER(10)                       NULL, -- 답변자 고유번호
  ANSWER      CLOB                             NULL, -- 답변 내용
  ADATE       VARCHAR2(30)                     NULL, -- 답변 등록일
  ISDEL       CHAR(1)      DEFAULT 'N'     NOT NULL, -- 삭제여부(Y/N)
  DDATE       VARCHAR2(30)                     NULL, -- 삭제일시
  ISFAQ       CHAR(1)                      NOT NULL, -- 자주묻는질문 여부
  FILEYN      CHAR(1)      DEFAULT 'N'     NOT NULL, -- 첨부파일 여부
  VCNT        NUMBER(10,0) DEFAULT 0           NULL, -- 조회수
  ID          VARCHAR2(20)                 NOT NULL, -- 등록자 ID

  CONSTRAINT PK_QA PRIMARY KEY (NO),
  CONSTRAINT FK_QA_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO),
  CONSTRAINT FK_QA_ANO FOREIGN KEY (ANO) REFERENCES MANAGER (NO)
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
COMMENT ON COLUMN QA.ISFAQ   IS '자주묻는질문 여부';
COMMENT ON COLUMN QA.FILEYN  IS '첨부파일 여부';
COMMENT ON COLUMN QA.ID      IS '등록자 ID';

CREATE SEQUENCE QA_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 20
  NOCYCLE;

ALTER TABLE QA MODIFY MNO NULL; -- 자주묻는 질문 등록시 MNO NULL 처리

----------------------------------------------------------------------------------------
ALTER TABLE QA ADD (FILEYN CHAR(1));
COMMENT ON COLUMN QA.FILEYN IS '첨부파일 존재여부 (Y/N)';

UPDATE QA SET FILEYN = 'N' WHERE FILEYN IS NULL;
SELECT FILEYN FROM QA WHERE FILEYN IS NULL;
COMMIT;

SELECT NO, ISDEL, ISFAQ FROM QA ORDER BY NO;



-----------------------------------------------------------------------------------------
INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 1, '관제 신청은 어떻게 하나요?',
  '스터디카페 무인 관제 신청 절차와 필요 서류가 궁금합니다.', '2026-08-10', '1234', 2, 'N', NULL, 1,
  '마이페이지 > 관제신청에서 사업자등록증을 첨부하여 신청하시면 영업일 기준 1일 이내 승인됩니다.', '2026-08-10', 'N', NULL, 'Y', 'Y');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 2, '특정 시간대 영상 요청 드립니다',
  '8/9 22시~23시 사이 카페 출입구 영상을 확인하고 싶습니다.', '2026-08-09', '1234', 1, 'Y', NULL, NULL,
  NULL, NULL, 'N', NULL, 'N', 'N');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 3, 'CCTV 1대가 오프라인으로 표시돼요',
  '2번 카메라가 어제부터 계속 오프라인 상태로 뜹니다. 전원은 정상입니다.', '2026-08-08', '1234', 0, 'N', NULL, NULL,
  NULL, NULL, 'N', NULL, 'N', 'Y');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 0, '구독 플랜 변경 시 위약금이 있나요?',
  '현재 베이직 플랜인데 프리미엄으로 변경 시 위약금이 있나요?', '2026-08-07', '1234', 2, 'N', 1, 1,
  '월 단위 구독은 위약금 없이 즉시 변경 가능하며, 차액은 일할 계산되어 다음 결제일에 반영됩니다.', '2026-08-07', 'N', NULL, 'Y', 'N');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 1, '2호점 추가 관제 신청',
  '2호점(강남점) CCTV 4대에 대한 관제 신청서를 첨부합니다.', '2026-08-06', '1234', 1, 'N', NULL, NULL,
  NULL, NULL, 'N', NULL, 'N', 'Y');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 3, '알림이 너무 늦게 와요',
  '이상행동 감지 후 푸시 알림이 5분 이상 늦게 오는 것 같습니다.', '2026-08-05', '1234', 2, 'N', NULL, 1,
  '확인 결과 일시적 서버 지연이었으며 현재는 정상적으로 실시간 발송되고 있습니다.', '2026-08-05', 'N', NULL, 'N', 'N');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 0, '탈퇴 후 영상 데이터는 어떻게 되나요?',
  '서비스 해지 시 저장된 CCTV 영상이 바로 삭제되는지 궁금합니다.', '2026-08-04', '1234', 0, 'N', 2, NULL,
  NULL, NULL, 'N', NULL, 'Y', 'N');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 2, '어제 야간 도난 의심 영상 확인 요청',
  '재고 위치가 바뀐 것 같아 어제 새벽 시간대 영상 확인을 요청드립니다.', '2026-08-03', '1234', 1, 'Y', NULL, NULL,
  NULL, NULL, 'N', NULL, 'N', 'Y');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 3, '앱에서 로그인이 계속 풀려요',
  '모바일 앱에서 5분 정도만 지나면 자동으로 로그아웃되는 현상이 있습니다.', '2026-08-02', '1234', 2, 'N', NULL, 1,
  '해당 버그를 확인했으며 다음 앱 업데이트(v1.4.2)에서 수정될 예정입니다. 불편을 드려 죄송합니다.', '2026-08-02', 'N', NULL, 'N', 'N');

INSERT INTO QA (NO, MNO, TYPE, TITLE, CONTENT, CDATE, PW, STATUS, VMODE, VSEQ, ANO, ANSWER, ADATE, ISDEL, DDATE, ISFAQ, FILEYN)
VALUES (QA_SEQ.NEXTVAL, 1, 1, '요금 세금계산서 발행 요청',
  '이번 달 이용 요금에 대한 세금계산서 발행 부탁드립니다.', '2026-08-01', '1234', 0, 'N', NULL, NULL,
  NULL, NULL, 'N', NULL, 'N', 'N');

COMMIT;