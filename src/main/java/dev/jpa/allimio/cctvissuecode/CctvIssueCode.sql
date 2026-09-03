--------------------------------------------------------
-- CCTV_ISSUE_CODE (이상행동유형코드)
--
-- CCTV_ISSUE.CODE / CctvIssue.ts CODE_LABELS / CctvAdmin.ts 등 4곳에 흩어져 있던
-- "01~05" 하드코딩 매핑을 여기 하나의 참조 테이블로 이관합니다.
--------------------------------------------------------
DROP TABLE CCTV_ISSUE_CODE CASCADE CONSTRAINTS;

CREATE TABLE CCTV_ISSUE_CODE (
  CODE          VARCHAR2(2)                 NOT NULL, -- 이상행동유형코드 (PK, CCTV_ISSUE.CODE와 동일한 값 체계)
  CODE_NAME     VARCHAR2(50)                NOT NULL, -- 코드명 (예: 폭행)
  DESCRIPTION   VARCHAR2(200)                   NULL, -- 설명
  SEVERITY      NUMBER(1)       DEFAULT 1   NOT NULL, -- 심각도 (1: 낮음 ~ 3: 높음)
  ORD           NUMBER(3)       DEFAULT 0   NOT NULL, -- 정렬 순서 (관리자 화면/드롭다운 노출 순서)
  USE_YN        VARCHAR2(1)     DEFAULT 'Y' NOT NULL, -- 사용여부 (Y/N)
  CDATE         VARCHAR2(19)                NOT NULL, -- 등록일시

  CONSTRAINT PK_CCTV_ISSUE_CODE PRIMARY KEY (CODE)
);

COMMENT ON TABLE  CCTV_ISSUE_CODE              IS '이상행동유형코드 (CCTV_ISSUE.CODE 참조 테이블)';
COMMENT ON COLUMN CCTV_ISSUE_CODE.CODE         IS '이상행동유형코드 (PK)';
COMMENT ON COLUMN CCTV_ISSUE_CODE.CODE_NAME    IS '코드명';
COMMENT ON COLUMN CCTV_ISSUE_CODE.DESCRIPTION  IS '설명';
COMMENT ON COLUMN CCTV_ISSUE_CODE.SEVERITY     IS '심각도 (1: 낮음 ~ 3: 높음)';
COMMENT ON COLUMN CCTV_ISSUE_CODE.ORD          IS '정렬 순서';
COMMENT ON COLUMN CCTV_ISSUE_CODE.USE_YN       IS '사용여부 (Y/N)';
COMMENT ON COLUMN CCTV_ISSUE_CODE.CDATE        IS '등록일시';

-- 기존 CctvIssue.ts / CctvIssueList.tsx(dbms·user) / jetson_worker(codes.py) 등에 하드코딩돼
-- 있던 5종 이상행동 코드를 그대로 시드 데이터로 이관합니다.
INSERT INTO CCTV_ISSUE_CODE (CODE, CODE_NAME, DESCRIPTION, SEVERITY, ORD, USE_YN, CDATE) VALUES
  ('01', '폭행',        '사람 간 신체적 폭력 행위 감지',                    3, 1, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO CCTV_ISSUE_CODE (CODE, CODE_NAME, DESCRIPTION, SEVERITY, ORD, USE_YN, CDATE) VALUES
  ('02', '기물파손',    '매장 내 집기·설비 파손 행위 감지',                 2, 2, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO CCTV_ISSUE_CODE (CODE, CODE_NAME, DESCRIPTION, SEVERITY, ORD, USE_YN, CDATE) VALUES
  ('03', '쓰러짐/응급', '사람이 쓰러지거나 응급 상황으로 추정되는 자세 감지', 3, 3, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO CCTV_ISSUE_CODE (CODE, CODE_NAME, DESCRIPTION, SEVERITY, ORD, USE_YN, CDATE) VALUES
  ('04', '무단침입',    '영업시간 외 매장 내 인원 감지(무단 침입)',          2, 4, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO CCTV_ISSUE_CODE (CODE, CODE_NAME, DESCRIPTION, SEVERITY, ORD, USE_YN, CDATE) VALUES
  ('05', '장시간체류',  '동일 인물이 매장 내 비정상적으로 오래 체류',        1, 5, 'Y', TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
COMMIT;

-- ---------------------------------------------------------------------------
-- (선택) CCTV_ISSUE.CODE 참조 무결성 제약 추가.
-- CCTV_ISSUE에 이미 저장된 CODE 값이 전부 위 5종(01~05) 안에 들어오는지 먼저 확인한 뒤 실행하세요.
--
--   SELECT DISTINCT CODE FROM CCTV_ISSUE
--   WHERE CODE NOT IN (SELECT CODE FROM CCTV_ISSUE_CODE);   -- 0건이어야 안전
--
-- ALTER TABLE CCTV_ISSUE
--   ADD CONSTRAINT FK_CCTV_ISSUE_CODE FOREIGN KEY (CODE) REFERENCES CCTV_ISSUE_CODE(CODE);
-- ---------------------------------------------------------------------------
