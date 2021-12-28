FROM gradle:7.1.1-jdk8 AS build
COPY ./ /usr/src/cs6310
WORKDIR /usr/src/cs6310
RUN gradle build --no-daemon

FROM openjdk:17-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /usr/src/cs6310/build/libs/*.jar /app/spring-boot-application.jar
CMD ["java", "-jar", "/app/spring-boot-application.jar"]