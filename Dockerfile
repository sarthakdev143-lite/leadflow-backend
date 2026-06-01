FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress
RUN cp target/*.jar app.jar && rm -f target/*.jar.original

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S leadflow && adduser -S leadflow -G leadflow

WORKDIR /app

COPY --from=builder /app/app.jar app.jar
RUN chown leadflow:leadflow app.jar

USER leadflow

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
