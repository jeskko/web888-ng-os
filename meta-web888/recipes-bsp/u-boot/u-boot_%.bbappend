# SPDX-License-Identifier: GPL-3.0-or-later
# U-Boot for the Web-888's own SD boot: SPL (boot.bin, with the PS7
# initialisation of our Vivado design) and U-Boot (u-boot.img) with the
# board's device tree, the A/B boot chooser and the ADC clock set up
# (files/web888.env), the environment raw on the SD card.
#
# ps7/ps7_init_gpl.*: exported by web888-ng-hw's FPGA build (fpga.yml,
# from 3dc8d1f), the same PS configuration in the HF and VHF builds; two
# empty parameter lists made "(void)" for the compiler. Its DDR, clock
# and PLL tables match the stock first-stage loader's.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:${THISDIR}/files/ps7:${THISDIR}/../../files/dts:"

SRC_URI += " \
    file://0001-zynq-environment-in-raw-MMC-when-booting-from-SD.patch \
    file://web888.cfg \
    file://web888.env \
    file://zynq-web888.dts \
    file://zynq-web888-u-boot.dtsi \
    file://ps7_init_gpl.c \
    file://ps7_init_gpl.h \
"

do_configure:prepend() {
	cp ${UNPACKDIR}/zynq-web888.dts ${UNPACKDIR}/zynq-web888-u-boot.dtsi ${S}/arch/arm/dts/
	grep -q zynq-web888.dtb ${S}/arch/arm/dts/Makefile || \
		sed -i 's/^dtb-$(CONFIG_ARCH_ZYNQ) += \\$/&\n\tzynq-web888.dtb \\/' ${S}/arch/arm/dts/Makefile
	grep -q zynq-web888.dtb ${S}/arch/arm/dts/Makefile
	install -d ${S}/board/xilinx/zynq/zynq-web888
	cp ${UNPACKDIR}/ps7_init_gpl.c ${UNPACKDIR}/ps7_init_gpl.h ${S}/board/xilinx/zynq/zynq-web888/
	cp ${UNPACKDIR}/web888.env ${S}/board/xilinx/zynq/
}
