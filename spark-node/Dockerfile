FROM spark:latest

COPY run-spark-node.sh .

EXPOSE 4040 8081

ENTRYPOINT ["/bin/bash", "-c", "/run-spark-node.sh"]