FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests package \
    && cp target/job-radar-*.jar application.jar


FROM eclipse-temurin:17-jre-jammy AS runtime

LABEL org.opencontainers.image.title="JobRadar" \
      org.opencontainers.image.description="Job intelligence platform for data engineering and AI opportunities" \
      org.opencontainers.image.source="https://github.com/saveriobutright/JobRadar" \
      org.opencontainers.image.licenses="MIT"

RUN groupadd --system jobradar \
    && useradd \
        --system \
        --gid jobradar \
        --create-home \
        --home-dir /app \
        jobradar

WORKDIR /app

COPY --from=build \
    --chown=jobradar:jobradar \
    /workspace/application.jar \
    application.jar

USER jobradar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
