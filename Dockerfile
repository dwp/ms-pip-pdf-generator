FROM gcr.io/distroless/java17@sha256:1e4181aaff242e2b305bb4abbe811eb122d68ffd7fd87c25c19468a1bc387ce6
COPY target/ms-pip-pdf-generator-*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
