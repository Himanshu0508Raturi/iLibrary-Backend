# ---------- Build stage ----------
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /workspace

# copy pom first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# copy sources and build
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# copy the built jar (pick the jar by wildcard)
COPY --from=builder /workspace/target/*.jar app.jar

# (optional) run as non-root user
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
