# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

COPY src src
# war 플러그인 사용 중 → bootWar로 실행 가능한 war 생성
RUN ./gradlew bootWar --no-daemon -x test && cp build/libs/*.war app.war

# ---------- 2단계: 실행 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul

COPY --from=build /app/app.war app.war

EXPOSE 9102
ENTRYPOINT ["java", "-jar", "app.war"]