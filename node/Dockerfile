FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

COPY target/*.jar node-1.0-SNAPSHOT.jar

EXPOSE 8085

CMD ["java", "-jar", "node-1.0-SNAPSHOT.jar"]