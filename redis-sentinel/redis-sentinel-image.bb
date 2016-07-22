# Copyright Vecima Networks Inc. as an unpublished work.
# All Rights Reserved.
#
# The information contained herein is confidential property of
# Vecima Networks Inc. The use, copying, transfer or disclosure of
# such information is prohibited except by express written agreement
# with Vecima Networks Inc.

SUMMARY = "Redis Sentinel Rkt image"

require recipes-images/containers/controller-base-image.bb

APP_CMD = "/etc/init.d/redis-create-sentinel.sh"

IMAGE_INSTALL += "redis-sentinel"
