FROM gradle:8.14.3-jdk21-ubi AS build
WORKDIR /app
COPY . .
RUN gradle :example:server-with-camunda-extension-example:bootJar --no-daemon

FROM openjdk:21-ea-23
WORKDIR /app
COPY --from=build /app/example/server-with-camunda-extension-example/build/libs/server-with-camunda-extension-example-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]