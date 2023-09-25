FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

COPY target/*.jar dispatcher-1.0-SNAPSHOT.jar

EXPOSE 8084

CMD ["java", "-jar", "dispatcher-1.0-SNAPSHOT.jar"]