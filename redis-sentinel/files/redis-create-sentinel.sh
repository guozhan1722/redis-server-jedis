#!/bin/sh
# Copyright Vecima Networks Inc. as an unpublished work.
# All Rights Reserved.
#
# The information contained herein is confidential property of
# Vecima Networks Inc. The use, copying, transfer or disclosure of
# such information is prohibited except by express written agreement
# with Vecima Networks Inc.
#

# This script initializes and creates the redis sentinel with a master
# by the name of vmaster
#
# A pod must be created for the master in redis.yaml. it must has
# the environment variable MASTER=true.

REDIS_SERVER_PORT=6379
REDIS_SENTINEL_PORT=26379
REDIS_CONF="/etc/redis/redis.conf"
REDIS_SLAVE_CONF="/etc/redis/redis-slave.conf"
SENTINEL_CONF="/etc/redis/sentinel.conf"
HOST_IP=$(/sbin/ifconfig eth0| awk '/inet addr/{print substr($2,6)}') 
MASTER_IP=${HOST_IP}

. /etc/redis/redis-common.sh

add_hostname_to_hosts ${HOST_IP}

init_sentinel

if [ "${MASTER}" = "true" ]; then
	create_sentinel_conf $HOST_IP $REDIS_SENTINEL_PORT
	start_redis_sentnel_server ${REDIS_CONF} ${SENTINEL_CONF} 
else 
	while [ "${MASTER_IP}" == "${HOST_IP}" ]; do
		search_redis_master_addr
		sleep 3
	done

	### Start redis server as a slave node
	cp -f ${REDIS_CONF} ${REDIS_SLAVE_CONF} 
	echo "slaveof ${MASTER_IP} ${REDIS_SERVER_PORT}">>  ${REDIS_SLAVE_CONF}
	create_sentinel_conf ${MASTER_IP} ${REDIS_SERVER_PORT}
	start_redis_sentnel_server ${REDIS_SLAVE_CONF} ${SENTINEL_CONF} 
fi

### wait for redis-sentinel to complete (which should be never)
wait


