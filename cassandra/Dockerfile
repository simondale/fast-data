FROM alpine:3.7

RUN apk --update --no-cache add bash wget openjdk8-jre python2 \
 && wget http://mirror.ox.ac.uk/sites/rsync.apache.org/cassandra/3.11.2/apache-cassandra-3.11.2-bin.tar.gz \
 && tar xzf apache-cassandra-3.11.2-bin.tar.gz \
 && rm apache-cassandra-3.11.2-bin.tar.gz

EXPOSE 7000 7001 7199 9042 9160

COPY run-cassandra.sh .

ENTRYPOINT ["/bin/bash", "-c", "/run-cassandra.sh"]
