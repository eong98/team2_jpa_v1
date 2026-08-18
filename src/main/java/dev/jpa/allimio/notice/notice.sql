
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
  FILEYN   CHAR(1)      DEFAULT 'N'    NOT NULL, -- 첨부파일 존재여부 (Y/N)
  VMODE    CHAR(1)      DEFAULT 'Y'    NOT NULL, -- 출력모드 (Y/N)
  VSEQ     NUMBER(5)                   NOT NULL, -- 출력순서
  ISDEL    CHAR(1)      DEFAULT 'N'    NOT NULL, -- 삭제여부(Y/N)
  DDATE    VARCHAR2(30)                    NULL, -- 삭제일시
  ID       VARCHAR2(20)                NOT NULL, -- 등록자 ID

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
COMMENT ON COLUMN NOTICE.ISDEL   IS '삭제여부 (Y/N)';
COMMENT ON COLUMN NOTICE.DDATE   IS '삭제일시';
COMMENT ON COLUMN NOTICE.ID      IS '등록자 ID';

CREATE SEQUENCE NOTICE_SEQ
  START WITH 1
  INCREMENT BY 1
  MAXVALUE 9999999999
  CACHE 20
  NOCYCLE;

------------------------------------------------------------------------
INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 0, '8/5(수) 02:00~04:00 서버 점검 안내',
  '서비스 안정화를 위해 위 시간동안 CCTV 모니터링 및 알림 발송이 일시 중단됩니다.', '1234', '2026-08-03', 214, 'Y', 'N', 'Y', 1, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 2, '이상행동 유형에 ''흡연 감지''가 추가되었습니다',
  '무인 스터디카페 매장 내 흡연 행위를 자동으로 감지하는 기능이 추가되었습니다. 설정 > 감지유형에서 확인하세요.', '1234', '2026-07-29', 152, 'N', 'Y', 'Y', 2, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 1, '7월 구독 결제 관련 안내',
  '7월분 구독료 청구서가 등록하신 카드로 정상 결제되었습니다. 명세서는 마이페이지에서 확인 가능합니다.', '1234', '2026-07-20', 341, 'N', 'N', 'Y', 3, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 3, 'CCTV 연동 가이드 문서가 갱신되었습니다',
  'ONVIF 지원 카메라 등록 절차와 트러블슈팅 항목이 추가된 최신 가이드를 확인해보세요.', '1234', '2026-07-14', 88, 'N', 'Y', 'Y', 4, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 3, '모바일 알림 수신 설정 안내',
  '앱 > 설정 > 알림에서 이상행동 유형별로 푸시 알림 수신 여부를 개별 설정할 수 있습니다.', '1234', '2026-07-05', 63, 'N', 'N', 'Y', 5, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 2, '매장별 대시보드 위젯이 추가되었습니다',
  '여러 매장을 운영 중이신 사장님을 위해 매장별 실시간 현황 위젯을 대시보드에 추가했습니다.', '1234', '2026-06-28', 121, 'N', 'N', 'Y', 6, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 1, '개인정보처리방침 개정 안내',
  'CCTV 영상 보관기간 및 제3자 제공 항목이 일부 개정되었습니다. 시행일: 2026-07-01', '1234', '2026-06-19', 205, 'N', 'N', 'Y', 7, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 3, '이용가이드 오탈자 수정',
  '이용가이드 3장 ''알림 설정'' 항목의 오탈자를 수정했습니다.', '1234', '2026-06-10', 41, 'N', 'N', 'Y', 8, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 3, '6월 정기 점검 결과 안내',
  '6월 정기 점검을 통해 CCTV 스트리밍 지연 이슈를 해결했습니다.', '1234', '2026-06-02', 97, 'N', 'N', 'Y', 9, 'N', NULL);

INSERT INTO NOTICE (NO, ANO, TYPE, TITLE, CONTENT, PW, CDATE, VCNT, FIXYN, FILEYN, VMODE, VSEQ, ISDEL, DDATE)
VALUES (NOTICE_SEQ.NEXTVAL, 1, 0, '일부 지역 CCTV 스트리밍 지연 이슈 안내',
  '일부 통신사 회선 문제로 실시간 영상 지연이 발생하고 있습니다. 현재 긴급 조치 중입니다.', '1234', '2026-05-27', 276, 'Y', 'N', 'Y', 10, 'N', NULL);

COMMIT;


SELECT * FROM NOTICE 
ORDER BY FIXYN DESC, TYPE ASC, CDATE DESC;