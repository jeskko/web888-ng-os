# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Init for chain-loading web888-ng from the stock firmware"
DESCRIPTION = "/init of web888-chainboot-initramfs: mounts the squashfs \
root appended to the initramfs under a RAM overlay and starts systemd."
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245"

SRC_URI = "file://init"
S = "${UNPACKDIR}"

inherit allarch

do_install() {
	install -m 0755 ${UNPACKDIR}/init ${D}/init
	install -d ${D}/dev ${D}/proc ${D}/sys
}

FILES:${PN} = "/init /dev /proc /sys"
RDEPENDS:${PN} = "busybox"
