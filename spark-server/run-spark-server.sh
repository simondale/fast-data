#!/bin/bash
set -e

export SPARK_NO_DAEMONIZE=1
/spark-2.1.2-bin-hadoop2.7/sbin/start-master.sh
