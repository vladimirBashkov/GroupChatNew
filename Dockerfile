FROM maven:3.9.8-eclipse-temurin-21

ENV HOME=/app
RUN mkdir -p $HOME
ADD pom.xml $HOME
ADD ./src $HOME/src
RUN mvn -f $HOME/pom.xml clean package

WORKDIR $HOME

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "./target/GroupChatNew-0.0.1.jar" ]
