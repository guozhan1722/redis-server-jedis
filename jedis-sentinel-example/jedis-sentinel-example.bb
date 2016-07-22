# Copyright Vecima Networks Inc. as an unpublished work.
# All Rights Reserved.
#
# The information contained herein is confidential property of
# Vecima Networks Inc. The use, copying, transfer or disclosure of
# such information is prohibited except by express written agreement
# with Vecima Networks Inc.
#
# Recipe created by vcm-template
#
#

DESCRIPTION = "jedis-sentinel-example"

#adds yocto dependencies and is also used to search for jars
DEPENDS = "commons-cli jedis commons-pool2"
RDEPENDS_${PN} += "libcommons-cli-java libjedis-java libcommons-pool2-java"

#adds dependencies for jars that do not match their recipe name in depends
JARDEPENDS = ""

RCLASSPATH = "jedis.jar commons-pool2.jar"

SECTION = "controller"

PR = "r0"

do_configure_append() {
     echo "deplibs.classpath=${RCLASSPATH}" >> build.property
}

inherit vcmjava

