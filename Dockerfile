FROM gcr.io/distroless/java11@sha256:f88c393a67fff3f9b599eca0e90350ee27e96d3803cbc7742ec23de6a9d6dd7d
COPY target/ms-pip-pdf-generator-*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
