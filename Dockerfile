FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

COPY target/*.jar telegram-bot.jar

EXPOSE 8080

CMD ["java", "-jar", "telegram-bot.jar"]