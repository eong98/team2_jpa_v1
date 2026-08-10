CREATE TABLE MEMBER (
    NO               NUMBER(10)     NOT NULL,          -- 회원 번호
    ID               VARCHAR2(20)   NOT NULL UNIQUE,    -- 아이디
    PASSWORD         VARCHAR2(100)  NOT NULL,           -- 비밀번호
    MNAME            VARCHAR2(20)   NOT NULL,           -- 이름
    EMAIL            VARCHAR2(50)   NOT NULL,           -- 이메일
    PHONE            VARCHAR2(20)   NOT NULL,           -- 전화번호
    ZIPCODE          VARCHAR2(10)   NOT NULL,           -- 우편번호
    ADDR             VARCHAR2(100)  NOT NULL,           -- 기본 주소
    ADDR_DETAIL      VARCHAR2(100)  NOT NULL,           -- 상세 주소
    GRADE            NUMBER(2)      NOT NULL,           -- 등급
    CDATE            VARCHAR2(19)   NOT NULL,           -- 가입일
    STATUS           VARCHAR2(100)  DEFAULT 1 NOT NULL, -- 상태(0: 정지::사유, 1: 정상, 2: 탈퇴)
    UDATE            VARCHAR2(19)   NULL,               -- 정보 수정일
    TERMS_AGREE_YN   CHAR(1)        NOT NULL,           -- 이용약관 동의
    PRIVACY_AGREE_YN CHAR(1)        NOT NULL,           -- 개인정보 이용 동의
    NATION           VARCHAR2(20)   NOT NULL,           -- 국적
  
    PRIMARY KEY (NO)
);

CREATE SEQUENCE MEMBER_SEQ START WITH 1 INCREMENT BY 1;

ALTER TABLE MEMBER MODIFY (NO NUMBER(10));

delete
from member;

BEGIN
    FOR c IN (SELECT table_name, constraint_name 
              FROM user_constraints 
              WHERE constraint_type = 'R' AND status = 'ENABLED') LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE "' || c.table_name || '" DISABLE CONSTRAINT "' || c.constraint_name || '"';
    END LOOP;
END;



INSERT INTO MEMBER (
    NO, ID, PASSWORD, EMAIL, PHONE, 
    MNAME, ZIPCODE, ADDR, ADDR_DETAIL, CDATE, 
    GRADE, STATUS, TERMS_AGREE_YN, PRIVACY_AGREE_YN, NATION, 
    UDATE
) VALUES (
    1, 'testuser01', 'hashed_password_123', 'user01@email.com', '010-1234-5678',
    '홍길동', '12345', '서울특별시 서대문구 연희로', '100번지 2층', '2026-08-10 14:00:00',
    1, '1', 'Y', 'Y', 'KOREA',
    NULL -- UDATE는 Null이 가능하므로 수정 전에는 NULL을 넣습니다.
);

commit;

BEGIN
    FOR c IN (SELECT table_name, constraint_name 
              FROM user_constraints 
              WHERE constraint_type = 'R' AND status = 'DISABLED') LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE "' || c.table_name || '" ENABLE CONSTRAINT "' || c.constraint_name || '"';
    END LOOP;
END;
/