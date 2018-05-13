FROM alpine:3.7 AS build

RUN apk --update --no-cache add wget openjdk8 bash \
 && wget -q https://cocl.us/sbt01316tgz \
 && tar xzf sbt01316tgz \
 && rm sbt01316tgz \
 && mkdir /build

ENV PATH="/sbt/bin:${PATH}"
COPY . /build
WORKDIR /build
RUN sbt -J-Xms1024m -J-Xmx4g assembly

FROM spark
COPY --from=build /build/target/scala-2.11/stream-processing-assembly-*.jar /stream-processing.jar
ENTRYPOINT ["/spark-2.1.2-bin-hadoop2.7/bin/spark-submit", "--master", "spark://spark:7077", "--class", "fastdata.Program", "--conf", "spark.executor.memory=512m", "stream-processing.jar"]