FROM registry.gitlab.com/dwp/secure-development/registry-mirrors/chainguard/jre:openjdk-17.0.17@sha256:e965c461b7fef9203ff659c6164c458de74bd72594a87ece99259e0eb8af8d3c
COPY target/ms-pip-pdf-generator-*.jar /app.jar

COPY --from=eyq18885.live.dynatrace.com/linux/oneagent-codemodules:java / /
ENV LD_PRELOAD /opt/dynatrace/oneagent/agent/lib64/liboneagentproc.so

EXPOSE 8080
USER java
ENTRYPOINT ["java", "-jar", "/app.jar"]
