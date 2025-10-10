# user-service/Dockerfile
FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/user-service-*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
