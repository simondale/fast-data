#!/bin/bash

: ${SERVER="spark"}
: ${PORT="7077"}
: ${OPTIONS=""}
: ${RUN_SPARK="n"}

if [ "$RUN_SPARK" = 'y' ]; then
/spark-2.1.2-bin-hadoop2.7/sbin/start-slave.sh $OPTIONS spark://$SERVER:$PORT
fi

/zeppelin-0.7.3-bin-all/bin/zeppelin.sh

if [ "$RUN_SPARK" = 'y' ]; then
/spark-2.1.2-bin-hadoop2.7/sbin/stop-slave.sh
fi

