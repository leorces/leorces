FROM gradle:8.14.3-jdk21-ubi AS build
WORKDIR /app
COPY . .
RUN gradle :example:client-example:bootJar --no-daemon

FROM openjdk:21
WORKDIR /app
COPY --from=build /app/example/client-example/build/libs/client-example-0.1.3.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]