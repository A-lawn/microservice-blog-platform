FROM openjdk:17-jdk-slim AS builder

WORKDIR /workspace
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

FROM openjdk:17-jre-slim

LABEL maintainer="blog-platform"

WORKDIR /app
COPY --from=builder /workspace/app.jar app.jar

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m \
    -Xmx512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:ParallelGCThreads=2 \
    -XX:ConcGCThreads=1 \
    -XX:InitiatingHeapOccupancyPercent=45 \
    -XX:G1ReservePercent=10 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    -XX:+TieredCompilation \
    -XX:TieredStopAtLevel=1 \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:MinRAMPercentage=25.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]