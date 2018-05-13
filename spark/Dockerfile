FROM alpine:3.7

RUN     apk update \
     && apk add bash coreutils procps wget openjdk8-jre \
     && wget http://mirror.ox.ac.uk/sites/rsync.apache.org/spark/spark-2.1.2/spark-2.1.2-bin-hadoop2.7.tgz \
     && tar xzf spark-2.1.2-bin-hadoop2.7.tgz \
     && rm spark-2.1.2-bin-hadoop2.7.tgz

