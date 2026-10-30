# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Cloudflare Tunnel client: the Web-888 behind Cloudflare without a VPS"
DESCRIPTION = "Cloudflare's static ARM (hard float) release binary of \
cloudflared, checked by SHA-256. web888-setup writes its token from the \
cloudflared_token setting in web888.txt; cloudflared.service runs only \
when it is there. docs/tunnels.md has the Cloudflare side."
HOMEPAGE = "https://github.com/cloudflare/cloudflared"
LICENSE = "Apache-2.0"
# a bare binary: the licence text from the common licences
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = " \
    https://github.com/cloudflare/cloudflared/releases/download/${PV}/cloudflared-linux-armhf;downloadfilename=cloudflared-${PV}-linux-armhf;name=bin \
    file://cloudflared.service \
"
# the release's asset digest
SRC_URI[bin.sha256sum] = "c047e24af9ed956b55d514e3a0e2d8020e9c6d0629c4731c8ec08bd79a29a050"

S = "${UNPACKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = "cloudflared.service"

# a prebuilt Go binary (not stripped: about 37 MB)
do_configure[noexec] = "1"
do_compile[noexec] = "1"
INSANE_SKIP:${PN} += "already-stripped ldflags arch"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_install() {
	install -D -m 0755 ${UNPACKDIR}/cloudflared-${PV}-linux-armhf ${D}${bindir}/cloudflared
	install -D -m 0644 ${UNPACKDIR}/cloudflared.service ${D}${systemd_system_unitdir}/cloudflared.service
}

COMPATIBLE_HOST = "arm.*-linux"
