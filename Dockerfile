FROM gcr.io/distroless/java17@sha256:7cf2e8e0b219ac40f96643fbd9fc068c75c5e3ea7396584030250f9d1581ef80
COPY target/ms-pip-pdf-generator-*.jar /app.jar

COPY --from=pik94420.live.dynatrace.com/linux/oneagent-codemodules:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-jar", "/app.jar"]
