# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "web888-ng SDR server"
DESCRIPTION = "Runs the Web-888 receiver through /dev/zynqsdr (open or stock \
driver) and serves its channels (HPSDR Protocol 1) and the web UI (8073)."
HOMEPAGE = "https://github.com/jeskko/web888-ng"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=75d892af193fd5a298f724c4377d8f62"

SRC_URI = "git://github.com/jeskko/web888-ng.git;protocol=https;branch=main \
           file://web888d.service \
           file://web888d.default \
           "
# main after v0.1.0: configuration file (M3 step 1)
SRCREV = "7162e17cbc05f4880a40af1ff425f498e550dbc1"
PV = "0.1.0+git"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}"

inherit cargo cargo-update-recipe-crates systemd

require ${BPN}-crates.inc

# the workspace also holds test tools; only the daemon goes on the device
CARGO_BUILD_FLAGS += "--package web888d"

do_install:append() {
	install -d ${D}${systemd_system_unitdir} ${D}${sysconfdir}/default
	install -m 0644 ${UNPACKDIR}/web888d.service ${D}${systemd_system_unitdir}/
	install -m 0644 ${UNPACKDIR}/web888d.default ${D}${sysconfdir}/default/web888d
}

SYSTEMD_SERVICE:${PN} = "web888d.service"
RDEPENDS:${PN} += "web888-sdr kernel-module-web888-sdr web888-bitstream web888d-ui"
