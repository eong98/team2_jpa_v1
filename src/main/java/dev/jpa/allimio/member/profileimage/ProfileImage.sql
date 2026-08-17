DROP TABLE PROFILE_IMAGE;

CREATE TABLE PROFILE_IMAGE (
  NO              NUMBER(10)            NOT NULL,        -- 프로필 이미지 번호
  MNO             NUMBER(10)            UNIQUE NOT NULL,        -- 회원 번호
  STORE_FILENAME  VARCHAR2(500)         NULL,            -- 저장된 파일명
  UPLOAD_FILENAME VARCHAR2(500)         NULL,            -- 원본 파일명
  UPDATE_DATE     VARCHAR2(19)          NULL,            -- 이미지 업로드일

  PRIMARY KEY (NO),
  FOREIGN KEY (MNO) REFERENCES MEMBER(NO)
);

CREATE SEQUENCE PROFILE_IMAGE_SEQ START WITH 1 INCREMENT BY 1;