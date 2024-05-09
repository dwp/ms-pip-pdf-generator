FROM gcr.io/distroless/java17@sha256:009deffea52dc93a7563fd73ff55138fa02cdabe32c4defa8375ce1cee86ac4a
COPY target/ms-pip-pdf-generator-*.jar /app.jar

COPY --from=pik94420.live.dynatrace.com/linux/oneagent-codemodules:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
