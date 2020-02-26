FROM maven:3.6.2-jdk-12 as builder

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline

ADD ./ ./

RUN mvn install && \
  cp target/*jar-with-dependencies.jar ./app.jar

FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk-headless

WORKDIR /api/

COPY --from=builder /app/app.jar ./scraper.jar

ADD config.json config.json
ADD twitter4j.properties twitter4j.properties

CMD ["java", "-jar", "scraper.jar"]
