FROM maven:3.8.4-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package


FROM openjdk:11
COPY --from=build /home/app/target/NeologRecommender-jar-with-dependencies.jar /usr/local/lib/recommender.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/recommender.jar"]
