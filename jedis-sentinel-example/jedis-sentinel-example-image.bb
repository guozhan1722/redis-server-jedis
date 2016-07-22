# Copyright Vecima Networks Inc. as an unpublished work.
# All Rights Reserved.
#
# The information contained herein is confidential property of
# Vecima Networks Inc. The use, copying, transfer or disclosure of
# such information is prohibited except by express written agreement
# with Vecima Networks Inc.

SUMMARY = "Jedis Sentinel Example image"
require recipes-images/containers/controller-base-image.bb

APP_CMD = "sleep" 
APP_CMD_ARGS = "1000000"
IMAGE_INSTALL += "jedis-sentinel-example"
