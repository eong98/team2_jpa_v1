CREATE TABLE MANAGER(
    NO         NUMBER(10)    NOT NULL,          -- 관리자 번호
    ID         VARCHAR(20)   NOT NULL UNIQUE,   --관리자 아이디
    PASSWORD   VARCHAR(100)  NOT NULL,          -- 패스워드, 영숫자 조합
    MNAME      VARCHAR(20)   NOT NULL,          -- 성명, 한글 10자 저장 가능
    EMAIL      VARCHAR(50)   NOT NULL,          -- 이메일
    PHONE      VARCHAR(20)   NOT NULL,          -- 전화번호
    GRADE      NUMBER(2)     NOT NULL,          -- 관리자 등급
    STATUS     VARCHAR(100)  DEFAULT 1 NOT NULL,-- 상태( 0: 정지::사유, 1: 정상 )
    CDATE      VARCHAR(19)   NOT NULL,          -- 가입일
    UDATE      VARCHAR(19)   NULL,              -- 정보 수정일
    
    PRIMARY KEY (NO)
);

CREATE SEQUENCE SEQ_MANAGER_NO START WITH 1 INCREMENT BY 1;