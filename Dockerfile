FROM gcr.io/distroless/java17@sha256:68e2373f7bef9486c08356bd9ffd3b40b56e6b9316c5f6885eb58b1d9093b43d
COPY target/ms-pip-pdf-generator-*.jar /app.jar

COPY --from=pik94420.live.dynatrace.com/linux/oneagent-codemodules:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
