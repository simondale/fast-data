FROM spark:latest

COPY run-spark-server.sh .

EXPOSE 7077 8080

ENTRYPOINT ["/bin/bash", "-c", "/run-spark-server.sh"]
