FROM alpine:3.7

RUN     apk update \
     && apk add bash wget openjdk8-jre \
     && wget http://mirror.ox.ac.uk/sites/rsync.apache.org/kafka/1.1.0/kafka_2.11-1.1.0.tgz \
     && tar xzf kafka_2.11-1.1.0.tgz \
     && rm kafka_2.11-1.1.0.tgz

EXPOSE 9092

COPY run-kafka.sh .

ENTRYPOINT ["/bin/sh", "/run-kafka.sh"]