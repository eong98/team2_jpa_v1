
--------------------------------------------------------
-- CHAT_SESSION (챗봇상담)
--------------------------------------------------------
DROP TABLE CHAT_SESSION CASCADE CONSTRAINTS;

CREATE TABLE CHAT_SESSION (
  NO         VARCHAR2(36)                NOT NULL, -- 세션식별키 (UUID, PK) - 상담 1건 단위
  MNO        NUMBER(10)                      NULL, -- 회원번호 (비회원: NULL, GNO 부여) (FK -> MEMBER.NO)
  GNO        VARCHAR2(36)                    NULL, -- 비회원 식별용 UUID (MNO가 NULL일 때, 브라우저에서 발급해 재사용)
  CNO        NUMBER(10)      DEFAULT 0   NULL, -- 현재 위치한 옵션형 메뉴 (AI상담중인 경우 NULL) (FK -> CHAT_MENU.NO)
  MODE       NUMBER(1)       DEFAULT 0   NOT NULL, -- 0: 옵션형 진행중, 1: AI상담 진행중, 2: 종료
  CHANNEL    NUMBER(2)       DEFAULT 10  NOT NULL, -- 접속 채널 (10: WEB, 20: MOBILE)
  QNO        NUMBER(10)                      NULL, -- 관리자 연결시 생성될 QA 문의글 번호(FK -> QA.NO)
  CDATE      VARCHAR2(30)                NOT NULL, -- 상담 세션 시작일시
  UDATE      VARCHAR2(30)                NOT NULL, -- 마지막 활동 시각 (메시지 주고받을 때마다 갱신)
  CLOSEDAT   VARCHAR2(30)                    NULL, -- 실제 종료 시각 (MODE=2 전환 시점)
  CREASON    NUMBER(1)                       NULL, -- 세션 종료 사유 (0: 해결됨, 1: 관리자연결, 2: 자동종료)
  SATISFY    NUMBER(1)                       NULL, -- 상담 만족도 (0: 불만족, 1: 만족, NULL: 미응답)
  SREASON    NUMBER(1)                       NULL, -- 불만족 사유 코드 (0: 답변부정확, 1: 응답느림, 2: 원하는답못찾음, 9: 기타)
  SMEMO      VARCHAR2(500)                   NULL, -- 불만족 사유 기타 직접입력 (SREASON=9일 때만)

  CONSTRAINT PK_CHAT_SESSION PRIMARY KEY (NO),
  CONSTRAINT FK_CHAT_SESSION_MNO FOREIGN KEY (MNO) REFERENCES MEMBER (NO),
  CONSTRAINT FK_CHAT_SESSION_CNO FOREIGN KEY (CNO) REFERENCES CHAT_MENU (NO),
  CONSTRAINT FK_CHAT_SESSION_QNO FOREIGN KEY (QNO) REFERENCES QA (NO)
);

COMMENT ON TABLE  CHAT_SESSION          IS '챗봇상담 (상담 1건 단위 세션)';
COMMENT ON COLUMN CHAT_SESSION.NO       IS '세션식별키 (UUID, PK)';
COMMENT ON COLUMN CHAT_SESSION.MNO      IS '회원번호 (비회원: NULL, GNO 부여)';
COMMENT ON COLUMN CHAT_SESSION.GNO      IS '비회원 식별용 UUID (MNO가 NULL일 때 브라우저에서 발급해 재사용)';
COMMENT ON COLUMN CHAT_SESSION.CNO      IS '현재 위치한 옵션형 메뉴 (AI상담중인 경우 NULL)';
COMMENT ON COLUMN CHAT_SESSION.MODE     IS '진행모드 - 0: 옵션형 진행중, 1: AI상담 진행중, 2: 종료';
COMMENT ON COLUMN CHAT_SESSION.CHANNEL  IS '접속 채널 (10: WEB, 20: MOBILE)';
COMMENT ON COLUMN CHAT_SESSION.QNO      IS '관리자연결시 생성될 QA 문의글 번호(FK)';
COMMENT ON COLUMN CHAT_SESSION.CDATE    IS '상담 세션 시작일시';
COMMENT ON COLUMN CHAT_SESSION.UDATE    IS '마지막 활동 시각 (메시지 주고받을 때마다 갱신)';
COMMENT ON COLUMN CHAT_SESSION.CLOSEDAT IS '실제 종료 시각 (MODE=2 전환 시점)';
COMMENT ON COLUMN CHAT_SESSION.CREASON  IS '세션 종료 사유 (0: 해결됨, 1: 관리자연결, 2: 자동종료)';
COMMENT ON COLUMN CHAT_SESSION.SATISFY  IS '상담 만족도 (0: 불만족, 1: 만족, NULL: 미응답)';
COMMENT ON COLUMN CHAT_SESSION.SREASON  IS '불만족 사유 코드 (0: 답변부정확, 1: 응답느림, 2: 원하는답못찾음, 9: 기타)';
COMMENT ON COLUMN CHAT_SESSION.SMEMO    IS '불만족 사유 기타 직접입력 (SREASON=9일 때만)';

CREATE INDEX IX_CHAT_SESSION_MNO ON CHAT_SESSION (MNO);
CREATE INDEX IX_CHAT_SESSION_GNO ON CHAT_SESSION (GNO);
CREATE INDEX IX_CHAT_SESSION_UDATE ON CHAT_SESSION (UDATE);