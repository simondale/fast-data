FROM alpine:3.7

RUN     apk update \
     && apk add bash wget openjdk8-jre \
     && wget http://mirror.ox.ac.uk/sites/rsync.apache.org/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz \
     && tar xzf zookeeper-3.4.12.tar.gz \
     && rm zookeeper-3.4.12.tar.gz \
     && echo tickTime=2000 > /zookeeper-3.4.12/conf/zoo.cfg \
     && echo dataDir=/zookeeper-3.4.12/data >> /zookeeper-3.4.12/conf/zoo.cfg \
     && echo clientPort=2181 >> /zookeeper-3.4.12/conf/zoo.cfg

EXPOSE 2181

ENTRYPOINT ["/zookeeper-3.4.12/bin/zkServer.sh", "start-foreground"]
