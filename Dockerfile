FROM maven:3.6.2-jdk-12 as builder

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline

COPY ./ ./

RUN mvn package && \
  cp target/*jar-with-dependencies.jar ./app.jar

FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk-headless

WORKDIR /api/

COPY --from=builder /app/app.jar ./scraper.jar

CMD ["java", "-jar", "scraper.jar"]
