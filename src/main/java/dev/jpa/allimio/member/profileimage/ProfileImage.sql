DROP TABLE PROFILE_IMAGE;

CREATE TABLE PROFILE_IMAGE (
  MNO             NUMBER(10)            NOT NULL,        -- 회원 번호
  STORE_FILENAME  VARCHAR2(500)         NULL,            -- 저장된 파일명
  UPLOAD_FILENAME VARCHAR2(500)         NULL,            -- 원본 파일명
  UPDATE_DATE     VARCHAR2(19)          NULL,            -- 이미지 업로드일

  PRIMARY KEY (MNO),
  FOREIGN KEY (MNO) REFERENCES MEMBER(NO)
);