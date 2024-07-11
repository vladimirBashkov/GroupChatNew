FROM maven:3.9.8-eclipse-temurin-21

WORKDIR /app

COPY target/GroupChatNew-0.0.1.jar app.jar
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
