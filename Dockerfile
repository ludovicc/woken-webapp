FROM openjdk:8

RUN \
  curl -L -o sbt-1.1.2.deb http://dl.bintray.com/sbt/debian/sbt-1.1.2.deb && \
  dpkg -i sbt-1.1.2.deb && \
  rm sbt-1.1.2.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR /graphql

ADD . /graphql

CMD sbt run

