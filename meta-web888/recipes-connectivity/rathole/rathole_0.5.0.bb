# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "rathole client: a reverse tunnel from the Web-888 to a public VPS"
DESCRIPTION = "The rathole project's static ARMv7 (musl, hard float) release \
binary, checked by SHA-256; built with Noise encryption, without TLS, and \
packed with UPX. web888-setup writes its configuration from the rathole_* \
settings in web888.txt; rathole.service runs only when they are there. \
docs/tunnels.md has the VPS side."
HOMEPAGE = "https://github.com/rapiz1/rathole"
LICENSE = "Apache-2.0"
# the release zip holds the binary only
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    https://github.com/rapiz1/rathole/releases/download/v${PV}/rathole-armv7-unknown-linux-musleabihf.zip;downloadfilename=rathole-${PV}-armv7-unknown-linux-musleabihf.zip;subdir=rathole-${PV};name=bin \
    file://rathole.service \
"
# hashed here: the release (2023) has no checksums
SRC_URI[bin.sha256sum] = "e8662d80d2cc9acc5f8f4d8a1c1a5ff7717b2fa71919a405d0eed8b64c8c1d88"

S = "${UNPACKDIR}/rathole-${PV}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "rathole.service"

# a prebuilt binary, UPX-packed: no section headers for the ELF checks
do_configure[noexec] = "1"
do_compile[noexec] = "1"
INSANE_SKIP:${PN} += "already-stripped ldflags arch"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_install() {
	install -D -m 0755 ${S}/rathole ${D}${bindir}/rathole
	install -D -m 0644 ${UNPACKDIR}/rathole.service ${D}${systemd_system_unitdir}/rathole.service
}

COMPATIBLE_HOST = "arm.*-linux"
