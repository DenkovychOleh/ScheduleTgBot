## Використовуємо офіційний образ OpenJDK в якості базового образу
#FROM openjdk:8-jre-alpine
#
## Встановлюємо робочий каталог /app
#WORKDIR /app
#
## Копіюємо зібраний JAR файл додатку у контейнер у каталог /app
#COPY target/dispatcher-1.0-SNAPSHOT.jar /app/dispatcher.jar
#
## Відкриваємо порт 8084 для зовнішніх з'єднань
#EXPOSE 8084
#
## Запускаємо додаток при запуску контейнера
#CMD ["java", "-jar", "dispatcher.jar"]
FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

COPY target/*.jar dispatcher-1.0-SNAPSHOT.jar

EXPOSE 8084

CMD ["java", "-jar", "dispatcher-1.0-SNAPSHOT.jar"]