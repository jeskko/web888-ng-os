# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "chisel client: a reverse tunnel over HTTP(S) from the Web-888 to a public VPS"
DESCRIPTION = "The chisel project's static ARMv7 release binary, checked by \
SHA-256. web888-setup writes its settings from the chisel_* settings in \
web888.txt; chisel.service runs only when they are there. docs/tunnels.md \
has the VPS side."
HOMEPAGE = "https://github.com/jpillora/chisel"
LICENSE = "MIT"
# the release holds the binary only
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    https://github.com/jpillora/chisel/releases/download/v${PV}/chisel_${PV}_linux_armv7.gz;name=bin \
    file://chisel.service \
"
# from the release's chisel_${PV}_checksums.txt
SRC_URI[bin.sha256sum] = "47ba1886948a2a71d91b1bebd94783456c62aa5e839c7a44406d7bb6f5175858"

S = "${UNPACKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "chisel.service"

# a prebuilt Go binary
do_configure[noexec] = "1"
do_compile[noexec] = "1"
INSANE_SKIP:${PN} += "already-stripped ldflags arch"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_install() {
	install -D -m 0755 ${UNPACKDIR}/chisel_${PV}_linux_armv7 ${D}${bindir}/chisel
	install -D -m 0644 ${UNPACKDIR}/chisel.service ${D}${systemd_system_unitdir}/chisel.service
}

COMPATIBLE_HOST = "arm.*-linux"
