FROM openjdk:8-jre-alpine

ENV APPLICATION_USER ktor
ENV GOOGLE_APPLICATION_CREDENTIALS=/resources/cloud-database-service-account.json

RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY ./build/libs/akvo-exact-0.0.1.jar /app/akvo-exact-0.0.1.jar
WORKDIR /app

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-cp", "akvo-exact-0.0.1.jar:/resources","io.ktor.server.netty.EngineMain"]
