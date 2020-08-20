FROM openjdk:8-jre-alpine

WORKDIR /recommender

COPY target/NeologRecommender-jar-with-dependencies.jar /recommender
EXPOSE 8080

CMD java -jar NeologRecommender-jar-with-dependencies.jar
