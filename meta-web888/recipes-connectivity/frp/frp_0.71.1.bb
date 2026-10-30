# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "frp client (frpc): a reverse tunnel from the Web-888 to a public VPS"
DESCRIPTION = "The frp project's static ARM release binary of frpc, checked \
by SHA-256. web888-setup writes its configuration from the frp_* settings \
in web888.txt; frpc.service runs only when they are there. docs/frp.md \
has the VPS side."
HOMEPAGE = "https://github.com/fatedier/frp"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fa818a259cbed7ce8bc2a22d35a464fc"

SRC_URI = " \
    https://github.com/fatedier/frp/releases/download/v${PV}/frp_${PV}_linux_arm.tar.gz;name=frp \
    file://frpc.service \
"
# from the release's frp_sha256_checksums.txt
SRC_URI[frp.sha256sum] = "8eb6fe66a3b36db24ca0de44fb01e6742d0ff60e30f462c9662f41813bbf4376"

S = "${UNPACKDIR}/frp_${PV}_linux_arm"

inherit systemd

SYSTEMD_SERVICE:${PN} = "frpc.service"

# a prebuilt binary: nothing to configure or build, no ELF checks that
# would trip on a stripped Go executable
do_configure[noexec] = "1"
do_compile[noexec] = "1"
INSANE_SKIP:${PN} += "already-stripped ldflags arch"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_install() {
	install -D -m 0755 ${S}/frpc ${D}${bindir}/frpc
	install -D -m 0644 ${UNPACKDIR}/frpc.service ${D}${systemd_system_unitdir}/frpc.service
}

COMPATIBLE_HOST = "arm.*-linux"
