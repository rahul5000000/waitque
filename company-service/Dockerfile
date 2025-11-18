# company-service/Dockerfile
FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/company-service-*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
