# ===== 런타임 이미지(경량 JRE 21) =====
FROM eclipse-temurin:21-jre-alpine

# 컨테이너 안 작업 디렉터리
WORKDIR /app

# 빌드된 JAR 파일 복사
# GitHub Actions에서 `./gradlew clean build -x test` 실행 후
# build/libs 밑에 생성되는 JAR을 가져온다.
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 애플리케이션 포트 (스프링 서버 포트와 맞추기)
EXPOSE 8080

# 필요하면 JVM 옵션도 여기에 추가 가능 (-Xms, -Xmx 등)
ENTRYPOINT ["java","-jar","/app/app.jar"]
