FROM spark:latest

RUN     wget http://mirror.ox.ac.uk/sites/rsync.apache.org/cassandra/3.11.2/apache-cassandra-3.11.2-bin.tar.gz \
     && tar xzf apache-cassandra-3.11.2-bin.tar.gz \
     && rm apache-cassandra-3.11.2-bin.tar.gz

EXPOSE 7000 7001 7199 9042 9160

COPY run-spark-cassandra.sh .

ENTRYPOINT ["/bin/bash", "-c", "/run-spark-cassandra.sh"]
