# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Open Web-888 FPGA bitstreams (web888-ng-hw release)"
DESCRIPTION = "HF and VHF bitstreams built by the web888-ng-hw CI and \
published as release assets; web888d loads the one for the variant \
(--bitstream auto) through the FPGA manager."
HOMEPAGE = "https://github.com/jeskko/web888-ng-hw"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245"

REL = "https://github.com/jeskko/web888-ng-hw/releases/download/v${PV}"
SRC_URI = "${REL}/websdr_hf.bit;name=hf;unpack=0 \
           ${REL}/websdr_vhf.bit;name=vhf;unpack=0 \
           "
# from the release's SHA256SUMS
SRC_URI[hf.sha256sum] = "6084026aded81fb22c8d9409995bda739ed4edf8eff85ed534e691d8c5ad2ddf"
SRC_URI[vhf.sha256sum] = "d201d910c0ede13987bbe4aee28e0079059da3deca1b94c1a96232a792244bd4"

S = "${UNPACKDIR}"

inherit allarch

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	install -d ${D}${datadir}/web888
	install -m 0644 ${UNPACKDIR}/websdr_hf.bit ${UNPACKDIR}/websdr_vhf.bit ${D}${datadir}/web888/
}

FILES:${PN} = "${datadir}/web888"
