FROM gcr.io/distroless/java11@sha256:e65ff03cf2bee3e2ea2a3fd26a49e5595be4f8d3df0e34454f32b06fc7a83753
COPY target/ms-pip-pdf-generator-*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
