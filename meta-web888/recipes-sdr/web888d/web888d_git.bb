# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "web888-ng SDR server"
DESCRIPTION = "Runs the Web-888 receiver through /dev/zynqsdr (open or stock \
driver) and serves its channels (HPSDR Protocol 1) and the web UI (8073)."
HOMEPAGE = "https://github.com/jeskko/web888-ng"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=75d892af193fd5a298f724c4377d8f62"

SRC_URI = "git://github.com/jeskko/web888-ng.git;protocol=https;branch=main \
           file://web888d.service \
           file://web888d-safe.service \
           file://web888d.default \
           "
# v0.17.0: the receiver as a clock (time-code stations to chrony, NTP for the LAN)
SRCREV = "16978d70aadf90c2aad935046b16afaae05baf5b"
PV = "0.17.0"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}"

inherit cargo cargo-update-recipe-crates systemd

require ${BPN}-crates.inc

# opus1 (the Opus encoder) declares Rust 1.98; wrynose's is 1.94.1. The
# patch drops its three cold_path() hints, the only newer thing it uses
SRC_URI += "file://opus1-0.3.0-rust-1.94.patch;patchdir=${CARGO_VENDORING_DIRECTORY}/opus1-0.3.0"

# the workspace also holds test tools; only the daemon goes on the device
CARGO_BUILD_FLAGS += "--package web888d"

do_install:append() {
	install -d ${D}${systemd_system_unitdir} ${D}${sysconfdir}/default
	install -m 0644 ${UNPACKDIR}/web888d.service ${D}${systemd_system_unitdir}/
	install -m 0644 ${UNPACKDIR}/web888d-safe.service ${D}${systemd_system_unitdir}/
	install -m 0644 ${UNPACKDIR}/web888d.default ${D}${sysconfdir}/default/web888d
	# the default config path leads to the config partition, so plain
	# `web888d passwd` (and --print-config) use the file the service does
	install -d ${D}${sysconfdir}/web888d
	ln -s /config/web888d.toml ${D}${sysconfdir}/web888d/web888d.toml
}

SYSTEMD_SERVICE:${PN} = "web888d.service web888d-safe.service"
RDEPENDS:${PN} += "web888-sdr kernel-module-web888-sdr web888-bitstream web888d-ui"
