FROM maven:3.9.14-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress --define skipTests package \
    && cp target/ms-reto-aws-*.jar /workspace/application.jar \
    && curl --fail --silent --show-error --location \
        --output /workspace/global-bundle.pem \
        https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem

FROM eclipse-temurin:25-jre-alpine-3.21 AS runtime

RUN addgroup --system --gid 10001 spring \
    && adduser --system --uid 10001 --ingroup spring spring

WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/application.jar ./application.jar
COPY --from=build --chown=spring:spring /workspace/global-bundle.pem ./certs/global-bundle.pem

USER 10001:10001

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --quiet --output-document=- http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/application.jar"]
