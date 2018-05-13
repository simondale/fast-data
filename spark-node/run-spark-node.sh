#!/bin/bash
set -e

: ${SERVER="spark"}
: ${PORT="7077"}
: ${OPTIONS=""}

export SPARK_NO_DAEMONIZE=1
/spark-2.1.2-bin-hadoop2.7/sbin/start-slave.sh $OPTIONS spark://$SERVER:$PORT
