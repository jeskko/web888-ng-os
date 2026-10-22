# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Web-888 SDR firmware image"
LICENSE = "MIT"

inherit core-image

# Read-only root; the configuration partition is mounted separately
# (read-write only while saving). RAUC A/B comes next.
IMAGE_FEATURES += "read-only-rootfs ssh-server-dropbear"

IMAGE_INSTALL += "web888-sdr"

IMAGE_LINGUAS = ""
IMAGE_FSTYPES += "squashfs-xz"

# web888d converts the bitstream for the FPGA manager and writes it to
# /run (tmpfs); the firmware loader finds it through this link.
ROOTFS_POSTPROCESS_COMMAND += "web888_firmware_link"
web888_firmware_link() {
	install -d ${IMAGE_ROOTFS}${nonarch_base_libdir}/firmware
	ln -sfn /run/web888/firmware ${IMAGE_ROOTFS}${nonarch_base_libdir}/firmware/web888
}
