# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Web-888 SDR firmware image"
LICENSE = "MIT"

inherit core-image

# Read-only root; the configuration partition is mounted separately.
IMAGE_FEATURES += "read-only-rootfs ssh-server-dropbear"

# e2fsck: the configuration partition is checked at boot (fstab pass 2).
# The kernel and the device tree in /boot: U-Boot loads them from the
# root slot it chose. web888-setup: web888.txt, the slot marked good.
IMAGE_INSTALL += "web888-sdr web888d e2fsprogs-e2fsck web888-setup \
                  kernel-image kernel-devicetree"

IMAGE_LINGUAS = ""
# the SD card image, compressed, with a block map for bmaptool
IMAGE_FSTYPES += "squashfs-xz wic.xz wic.bmap"
# (zynq-generic adds an emulator copy; tests/sd-image.sh of web888-ng-hw
# makes its own)
IMAGE_FSTYPES:remove = "wic.qemu-sd"

# the SD card's FAT: BOOT.BIN, u-boot.img (IMAGE_BOOT_FILES) and
# web888.txt
do_image_wic[depends] += "web888-setup:do_deploy"

ROOTFS_POSTPROCESS_COMMAND += "web888_firmware_link web888_ssh"

# web888d converts the bitstream for the FPGA manager and writes it to
# /run (tmpfs); the firmware loader finds it through this link.
web888_firmware_link() {
	install -d ${IMAGE_ROOTFS}${nonarch_base_libdir}/firmware
	ln -sfn /run/web888/firmware ${IMAGE_ROOTFS}${nonarch_base_libdir}/firmware/web888
}

# SSH: root's key from web888.txt (web888-setup writes it to /run), the
# host keys kept on the configuration partition
web888_ssh() {
	rm -rf ${IMAGE_ROOTFS}${ROOT_HOME}/.ssh
	ln -s /run/web888/ssh ${IMAGE_ROOTFS}${ROOT_HOME}/.ssh
	f=${IMAGE_ROOTFS}${sysconfdir}/default/dropbear
	if [ -e $f ]; then
		sed -i '/^DROPBEAR_RSAKEY_DIR=/d' $f
		echo "DROPBEAR_RSAKEY_DIR=/config/ssh" >> $f
	fi
}
