FROM gradle:8.14.3-jdk21-ubi AS build
WORKDIR /app
COPY . .
RUN gradle :example:server-with-camunda-extension-example:bootJar --no-daemon

FROM openjdk:21
WORKDIR /app
COPY --from=build /app/example/server-with-camunda-extension-example/build/libs/server-with-camunda-extension-example-0.1.3.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]