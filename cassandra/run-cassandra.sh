#!/bin/bash
set -e 

CASSANDRA_CONFIG=/apache-cassandra-3.11.2/conf

: ${CASSANDRA_CLUSTER:='Cassandra Cluster'}
sed -ri 's/(cluster_name:).*/\1 '"'$CASSANDRA_CLUSTER'"'/' "$CASSANDRA_CONFIG/cassandra.yaml"

: ${CASSANDRA_RPC_ADDRESS='0.0.0.0'}

: ${CASSANDRA_LISTEN_ADDRESS='auto'}
if [ "$CASSANDRA_LISTEN_ADDRESS" = 'auto' ]; then
	CASSANDRA_LISTEN_ADDRESS="$(hostname -i)"
fi

: ${CASSANDRA_BROADCAST_ADDRESS="$CASSANDRA_LISTEN_ADDRESS"}

if [ "$CASSANDRA_BROADCAST_ADDRESS" = 'auto' ]; then
	CASSANDRA_BROADCAST_ADDRESS="$(hostname -i)"
fi
: ${CASSANDRA_BROADCAST_RPC_ADDRESS:=$CASSANDRA_BROADCAST_ADDRESS}

if [ -n "${CASSANDRA_NAME:+1}" ]; then
	: ${CASSANDRA_SEEDS:="cassandra"}
fi
: ${CASSANDRA_SEEDS:="$CASSANDRA_BROADCAST_ADDRESS"}

sed -ri 's/(- seeds:) "127.0.0.1"/\1 "'"$CASSANDRA_SEEDS"'"/' "$CASSANDRA_CONFIG/cassandra.yaml"

for yaml in broadcast_address broadcast_rpc_address cluster_name endpoint_snitch listen_address num_tokens rpc_address start_rpc; do
	var="CASSANDRA_${yaml^^}"
	val="${!var}"
	if [ "$val" ]; then
		sed -ri 's/^(# )?('"$yaml"':).*/\2 '"$val"'/' "$CASSANDRA_CONFIG/cassandra.yaml"
	fi
done

for rackdc in dc rack; do
	var="CASSANDRA_${rackdc^^}"
	val="${!var}"
	if [ "$val" ]; then
		sed -ri 's/^('"$rackdc"'=).*/\1 '"$val"'/' "$CASSANDRA_CONFIG/cassandra-rackdc.properties"
	fi
done

: ${CASSANDRA_LOMEM:="false"}
: ${CASSANDRA_LOMEM_MAXHEAP:="64M"}
: ${CASSANDRA_LOMEM_NEWHEAP:="12M"}
: ${CASSANDRA_LOMEM_CONC_READS:="2"}
: ${CASSANDRA_LOMEM_CONC_WRITE:="2"}
: ${CASSANDRA_LOMEM_CONC_COUNT:="2"}
: ${CASSANDRA_LOMEM_RPC_MIN_THREADS:="1"}
: ${CASSANDRA_LOMEM_RPC_MAX_THREADS:="1"}
: ${CASSANDRA_LOMEM_CONC_COMPACTORS:="1"}
: ${CASSANDRA_LOMEM_COMPACTION_TP:="0"}
: ${CASSANDRA_LOMEM_COMPACTION_LM:="1"}
: ${CASSANDRA_LOMEM_RED_CACHE_AT:="0"}
: ${CASSANDRA_LOMEM_RED_CACHE_TO:="0"}
if [ "$CASSANDRA_LOMEM" = 'true' ]; then
	sed -ri 's/[#]*(MAX_HEAP_SIZE)=.*/\1="'$CASSANDRA_LOMEM_MAXHEAP'"/' "$CASSANDRA_CONFIG/cassandra-env.sh"
	sed -ri 's/[#]*(HEAP_NEWSIZE)=.*/\1="'$CASSANDRA_LOMEM_NEWHEAP'"/' "$CASSANDRA_CONFIG/cassandra-env.sh"
	
	sed -ri 's/[#]*(concurrent_reads):.*/\1: "'$CASSANDRA_LOMEM_CONC_READS'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(concurrent_writes):.*/\1: "'$CASSANDRA_LOMEM_CONC_WRITE'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(concurrent_counter_writes):.*/\1: "'$CASSANDRA_LOMEM_CONC_COUNT'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(rpc_min_threads):.*/\1: "'$CASSANDRA_LOMEM_RPC_MIN_THREADS'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(rpc_max_threads):.*/\1: "'$CASSANDRA_LOMEM_RPC_MAX_THREADS'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(concurrent_compactors):.*/\1: "'$CASSANDRA_LOMEM_CONC_COMPACTORS'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(compaction_throughput_mb_per_sec):.*/\1: "'$CASSANDRA_LOMEM_COMPACTION_TP'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(in_memory_compaction_limit_in_mb):.*/\1: "'$CASSANDRA_LOMEM_COMPACTION_LM'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(reduce_cache_sizes_at):.*/\1: "'$CASSANDRA_LOMEM_RED_CACHE_AT'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
	sed -ri 's/[#]*(reduce_cache_capacity_to):.*/\1: "'$CASSANDRA_LOMEM_RED_CACHE_TO'"/' "$CASSANDRA_CONFIG/cassandra.yaml"
fi

/apache-cassandra-3.11.2/bin/cassandra -fR
