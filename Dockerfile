FROM kamackay/alpine

WORKDIR /TwitterScraper

ADD ./ ./

#RUN rm -rf ./target

RUN mvn clean && mvn install

CMD ["java", "-jar", "target/twitterscraper-1.0.0.jar"]
