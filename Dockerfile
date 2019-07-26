FROM kamackay/alpine

RUN mkdir -p /opt/project
COPY target/*jar-with-dependencies.jar /opt/project/scraper.jar

WORKDIR /opt/project
EXPOSE 5656
ADD config.json config.json
ADD twitter4j.properties twitter4j.properties

CMD ["java", "-jar", "scraper.jar"]
