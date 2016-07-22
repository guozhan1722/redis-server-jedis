# Copyright Vecima Networks Inc. as an unpublished work.
# All Rights Reserved.
#
# The information contained herein is confidential property of
# Vecima Networks Inc. The use, copying, transfer or disclosure of
# such information is prohibited except by express written agreement
# with Vecima Networks Inc.

SUMMARY = "Redis Sentinel"
DESCRIPTION = "Sets redis sentinel up to run on kubernetes"
LICENSE = "CLOSED"
DEPENDS = "redis"
RDEPENDS_${PN} += "redis-clustermode openssl curl"

SRC_URI = "file://redis.conf \
		   file://sentinel.conf \
           file://redis-create-sentinel.sh \
           file://redis-common.sh"

do_compile[noexec] = "1"

do_install() {
    install -d ${D}/${sysconfdir}/redis-data
    install -d ${D}/${sysconfdir}/redis
    install -m 0644 ${WORKDIR}/redis.conf ${D}/${sysconfdir}/redis/redis.conf
	install -m 0644 ${WORKDIR}/sentinel.conf ${D}/${sysconfdir}/redis/sentinel.conf
    install -m 0644 ${WORKDIR}/redis-common.sh ${D}/${sysconfdir}/redis/redis-common.sh
    install -d ${D}/${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/redis-create-sentinel.sh ${D}/${sysconfdir}/init.d/redis-create-sentinel.sh
}
