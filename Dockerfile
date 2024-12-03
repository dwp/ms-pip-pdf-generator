FROM gcr.io/distroless/java17@sha256:38e4b51e5fbd44e5b3f8d77bcc8ae573f265174249dad7316aa3a9ce0ada0cfc
COPY target/ms-pip-pdf-generator-*.jar /app.jar

COPY --from=pik94420.live.dynatrace.com/linux/oneagent-codemodules:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
