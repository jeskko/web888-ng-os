# SPDX-License-Identifier: GPL-3.0-or-later
FILESEXTRAPATHS:prepend := "${THISDIR}/files:${THISDIR}/../../files/dts:"
SRC_URI += "file://web888.cfg file://zynq-web888.dts"

# the board's device tree (KERNEL_DEVICETREE in the machine config)
do_configure:append() {
	cp ${UNPACKDIR}/zynq-web888.dts ${S}/arch/arm/boot/dts/xilinx/
}
