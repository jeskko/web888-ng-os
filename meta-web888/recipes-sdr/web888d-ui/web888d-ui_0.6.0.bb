# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "web888d web UI"
DESCRIPTION = "The browser UI served by web888d (TypeScript/Preact, built \
by the web888-ng CI and published as a release asset), so the image build \
needs no Node toolchain."
HOMEPAGE = "https://github.com/jeskko/web888-ng"
LICENSE = "GPL-3.0-or-later & MIT"
LICENSE:${PN} = "GPL-3.0-or-later & MIT"
# the UI is GPL-3.0+; the bundled Preact and @preact/signals are MIT
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245 \
                    file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://github.com/jeskko/web888-ng/releases/download/v${PV}/web888d-ui-v${PV}.tar.gz;subdir=ui"
# from the release's SHA256SUMS
SRC_URI[sha256sum] = "5489ec0259640bf0adcc5702d59e5d5ad954bcacc0294f70ee64dc954a2efe53"

S = "${UNPACKDIR}/ui"

inherit allarch

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	install -d ${D}${datadir}/web888d/ui
	install -m 0644 ${S}/*.html ${S}/*.js ${S}/*.css ${S}/*.wasm ${D}${datadir}/web888d/ui/
}

FILES:${PN} = "${datadir}/web888d/ui"
