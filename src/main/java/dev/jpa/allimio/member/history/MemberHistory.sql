CREATE TABLE MEMBER_HISTORY (
    NO           NUMBER(10)    NOT NULL, -- 로그인 기록 번호
    LOGIN_ID     VARCHAR2(20)  NOT NULL, -- 시도한 아이디
    LOGIN_DATE   VARCHAR2(19)  NOT NULL, -- 로그인 시도 일시 (기존 19에서 20으로 수정)
    LOGIN_RESULT NUMBER(1)     NOT NULL, -- 로그인 시도 결과 (0:실패, 1:성공)
    MNO          NUMBER(10)     NOT NULL, -- 회원 번호
    FAIL_REASON  VARCHAR2(50)  NULL,     -- 실패 사유 (기존 100에서 50으로 수정)
    IP_ADDR      VARCHAR2(50)  NULL,     -- IP 주소 (기존 24에서 50으로 수정)
    
    PRIMARY KEY (NO)

);

CREATE SEQUENCE MEMBER_HISTORY_SEQ START WITH 1 INCREMENT BY 1;
