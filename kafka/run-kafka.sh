#!/bin/sh

: ${ADVERTISED_LISTENERS=PLAINTEXT://$(hostname -i):9092}

/kafka_2.11-1.1.0/bin/zookeeper-shell.sh zookeeper:2181 create /kafka null
/kafka_2.11-1.1.0/bin/kafka-server-start.sh /kafka_2.11-1.1.0/config/server.properties --override zookeeper.connect=zookeeper:2181/kafka --override advertised.listeners=$ADVERTISED_LISTENERS
