FROM ubuntu:18.04
VOLUME /result
RUN apt-get update && apt-get -y install maven openjdk-11-jdk-headless
COPY ./ src/
WORKDIR /src
CMD [ "bash", "-c", "mvn clean test && cp target/surefire-reports/*.xml /result/ && chown -R ${RUN_AS_UID:-0} /result/" ]