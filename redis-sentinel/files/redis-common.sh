# Copyright Vecima Networks Inc. as an unpublished work.
# All Rights Reserved.
#
# The information contained herein is confidential property of
# Vecima Networks Inc. The use, copying, transfer or disclosure of
# such information is prohibited except by express written agreement
# with Vecima Networks Inc.
#

# This script implements some functions to redis sentinel setup 

init_sentinel()
{
	### Remove redis server and redis sentinel before created
	redis_pid=$(ps aux |grep -w redis-server |grep -v grep|awk {'print $1'})
	sentinel_pid=$(ps aux |grep -w redis-sentinel |grep -v grep|awk {'print $1'})

	if [ "${redis_pid}" != "" ]; then
		kill -9 $redis_pid > /dev/null 2>&1
	fi

	if [ "${sentinel_pid}" != "" ]; then
		kill -9 $sentinel_pid > /dev/null 2>&1
	fi
}

add_hostname_to_hosts()
{	
	HOST_IP=$1
	HOST_NAME=$(hostname)
	
	### Dont add it repeatedly
	need_add=$(grep -i "${HOST_IP} ${HOST_NAME}" /etc/hosts)
	if [ "$?" != "0" ]; then
		echo "Adding entry '${HOST_IP}' for '${HOST_NAME}' to /etc/hosts"
		echo "${HOST_IP} ${HOST_NAME}" >> /etc/hosts
	fi
}

### Writes to both stdout and syslog. Stdout output is read by the kubectl logs command
write_to_log() {
    level=$1
    msg=$2

    # make sure special characters get processed correctly
    echo -e ${level}: ${msg}
    logger -p local0.${level} -t redis-sentinel[${HOSTNAME}] $(echo -e ${msg})
}

search_redis_master_addr()
{
	EP_FILE="redis-sentinel"
	EP_DIR="/tmp"
	INIT_SVC_ENDPOINTS="https://kubernetes/api/v1/namespaces/default/endpoints/${EP_FILE}"
	CONNECT_RETRIES=10

	### Find out how many nodes here
    connect_retries=0
    token=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
    while [ ${connect_retries} -lt ${CONNECT_RETRIES} ]; do
        curl -k -H "Authorization: Bearer ${token}" ${INIT_SVC_ENDPOINTS} -o ${EP_DIR}/${EP_FILE} > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            break
        fi
        connect_retries=$((connect_retries+1))
        sleep 1
    done

    if [ ${connect_retries} -ge ${CONNECT_RETRIES} ]; then
        write_to_log "err" "Cannot connect to API server. Max retries achieved. Exiting."
        exit 1
    fi

    nodes=$(grep '\"ip\"' ${EP_DIR}/${EP_FILE} | egrep -o '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+')

    ### Find out Master IP address through the vmaster group
    for n in ${nodes}; do
    	### Connection test here
        connect_result=$(redis-cli -h ${n} -p ${REDIS_SENTINEL_PORT} ping)
 		if [ "${connect_result}" = "PONG" ]; then
 			m_addr=$(redis-cli -h ${n} -p ${REDIS_SENTINEL_PORT} sentinel get-master-addr-by-name vmaster)
 			
 			### Separate the IP and port.  
 			master_ip=$(echo $m_addr|egrep -o '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+')
 			
 			### Sometimes this master shuts down but sentinel does not know. we need test it again
	        connect_result=$(redis-cli -h ${master_ip} ping)
	 		if [ "${connect_result}" = "PONG" ]; then
	 			MASTER_IP=$(echo $m_addr|egrep -o '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+')
				break;
	 		fi
 		fi       
    done
    
    rm -rf ${EP_DIR}/${EP_FILE}
}

create_sentinel_conf(){
	master_ip=$1
	master_port=$2
	
	echo "sentinel monitor vmaster ${master_ip} ${master_port} 2" > ${SENTINEL_CONF}
  	echo "sentinel down-after-milliseconds vmaster 60000" >> ${SENTINEL_CONF}
  	echo "sentinel failover-timeout vmaster 180000" >> ${SENTINEL_CONF}
  	echo "sentinel parallel-syncs vmaster 1" >> ${SENTINEL_CONF}
	echo "pidfile /var/run/redis-sentinel.pid" >> ${SENTINEL_CONF}
	echo "port ${REDIS_SENTINEL_PORT}" >> ${SENTINEL_CONF}
	### When we need monitor the sentinel performance, 
	### we can turn them off
	#echo "logfile sentinel.log" >>${SENTINEL_CONF}
	#echo "dir /etc/redis-data" >>	${SENTINEL_CONF}
	#echo "daemonize yes" >> ${SENTINEL_CONF}
}

start_redis_sentnel_server(){
	redis_conf=$1
	sentinel_conf=$2
	
	redis-server ${redis_conf}
	redis-sentinel ${sentinel_conf}
}



