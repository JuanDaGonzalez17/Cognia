FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/

RUN chmod +x gradlew \
	&& ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system cognia \
	&& useradd --system --gid cognia --home-dir /app cognia

COPY --from=builder --chown=cognia:cognia /workspace/build/libs/*.jar /app/app.jar

USER cognia

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
