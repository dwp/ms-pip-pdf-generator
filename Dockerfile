FROM gcr.io/distroless/java17@sha256:2f01c2ff0c0db866ed73085cf1bb5437dd162b48526f89c1baa21dd77ebb5e6d
COPY target/ms-pip-pdf-generator-*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
