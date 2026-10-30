# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Open Web-888 FPGA bitstreams (web888-ng-hw release)"
DESCRIPTION = "HF, VHF and channelizer bitstreams built by the \
web888-ng-hw CI and published as release assets; web888d loads the one \
for the variant (--bitstream auto), or the channelizer when web888.txt \
says so, through the FPGA manager."
HOMEPAGE = "https://github.com/jeskko/web888-ng-hw"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245"

# HF and VHF from v0.2.0, the hardware-tested HF (v0.3.0's rebuilds share
# changed code with the channelizer and haven't run on a board); the
# channelizer from v0.3.0
REL = "https://github.com/jeskko/web888-ng-hw/releases/download"
SRC_URI = "${REL}/v0.2.0/websdr_hf.bit;name=hf;unpack=0 \
           ${REL}/v0.2.0/websdr_vhf.bit;name=vhf;unpack=0 \
           ${REL}/v${PV}/websdr_pfb.bit;name=pfb;unpack=0 \
           "
# from the releases' SHA256SUMS
SRC_URI[hf.sha256sum] = "ef9740501352995128c586fad718b35c2b8866c54ff981978b160b0a8814a2f4"
SRC_URI[vhf.sha256sum] = "62df1ddcb1bb86e5f59430ccfef2eb298e3bf0f11fef7e14738f59218320e3ad"
SRC_URI[pfb.sha256sum] = "71cdc3e5a85bc8f835ac0ede89595e946c86a54a004a7e5adc1b804d5d73f910"

S = "${UNPACKDIR}"

inherit allarch

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	install -d ${D}${datadir}/web888
	install -m 0644 ${UNPACKDIR}/websdr_hf.bit ${UNPACKDIR}/websdr_vhf.bit \
		${UNPACKDIR}/websdr_pfb.bit ${D}${datadir}/web888/
}

FILES:${PN} = "${datadir}/web888"
