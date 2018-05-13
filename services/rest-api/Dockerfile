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

FROM alpine:3.7
RUN apk --update --no-cache add openjdk8-jre
COPY --from=build /build/target/scala-2.11/rest-api-assembly-*.jar /rest-api.jar
EXPOSE 5000
ENTRYPOINT ["/usr/bin/java", "-jar", "/rest-api.jar"]
