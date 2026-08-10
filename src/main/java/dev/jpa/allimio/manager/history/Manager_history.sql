CREATE TABLE MANAGER_HISTORY (
    NO           NUMBER(10)    NOT NULL, -- 로그인 기록 번호
    LOGIN_ID     VARCHAR2(20)  NOT NULL, -- 시도한 아이디
    LOGIN_DATE   VARCHAR2(20)  NOT NULL, -- 로그인 시도 일시
    LOGIN_RESULT NUMBER(1)     NOT NULL, -- 로그인 시도 결과 (0:실패, 1:성공)
    MNNO         NUMBER(5)     NULL,     -- 관리자 번호 (물리적 FK 끊음, 실패시 NULL/성공시 관리자번호 저장)
    FAIL_REASON  VARCHAR2(50)  NULL,     -- 실패 사유
    IP_ADDR      VARCHAR2(50)  NULL,     -- IP 주소
    
    PRIMARY KEY (NO)
);

CREATE SEQUENCE MANAGER_HISTORY_SEQ START WITH 1 INCREMENT BY 1;