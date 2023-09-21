## Використовуємо офіційний образ OpenJDK в якості базового образу
#FROM openjdk:8-jre-alpine
#
## Встановлюємо робочий каталог /app
#WORKDIR /app
#
## Копіюємо зібраний JAR файл додатку у контейнер у каталог /app
#COPY target/node-1.0-SNAPSHOT.jar /app/node.jar
#
## Відкриваємо порт 8085 для зовнішніх з'єднань
#EXPOSE 8085
#
## Запускаємо додаток при запуску контейнера
#CMD ["java", "-jar", "node.jar"]
FROM openjdk:8-jre-alpine

RUN mkdir /app

WORKDIR /app

COPY target/*.jar node-1.0-SNAPSHOT.jar

EXPOSE 8085

CMD ["java", "-jar", "node-1.0-SNAPSHOT.jar"]