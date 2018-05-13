FROM spark:latest

RUN     wget http://mirror.ox.ac.uk/sites/rsync.apache.org/zeppelin/zeppelin-0.7.3/zeppelin-0.7.3-bin-all.tgz \
     && tar xzf zeppelin-0.7.3-bin-all.tgz \
     && rm zeppelin-0.7.3-bin-all.tgz

ENV SPARK_HOME /spark-2.1.2-bin-hadoop2.7

EXPOSE 8080

COPY run-spark-zeppelin.sh .

ENTRYPOINT ["/bin/bash", "-c", "/run-spark-zeppelin.sh"]
