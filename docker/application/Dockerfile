FROM ubuntu:18.04
VOLUME /results
RUN apt-get update && apt-get -y install maven openjdk-11-jdk-headless
COPY ./ src/
WORKDIR /src
RUN mvn clean package -DskipTests

FROM openjdk:11
EXPOSE 80
COPY --from=0 /src/target/metadata-api-0.1-SNAPSHOT.jar /app.jar
CMD java -jar /app.jar