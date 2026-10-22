# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Open kernel driver for the Web-888 SDR programmable logic"
DESCRIPTION = "Drop-in for the closed zynqsdr driver: /dev/zynqsdr with the \
stock ioctl ABI, plus poll(), RX overrun detection and statistics for the \
open web888-ng bitstream."
HOMEPAGE = "https://github.com/jeskko/web888-ng-hw"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://web888_sdr.c;beginline=1;endline=1;md5=7226e442a172bcf25807246d7ef1eba1"

SRC_URI = "git://github.com/jeskko/web888-ng-hw.git;protocol=https;branch=main"
SRCREV = "323f97b47c5f26997e667fc0da0762edb2be4dc4"
PV = "0.1+git"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}/driver"

inherit module

# loaded through the DT modalias (rx888,zynqsdr / web888-ng,sdr)
RPROVIDES:${PN} += "kernel-module-web888-sdr"
