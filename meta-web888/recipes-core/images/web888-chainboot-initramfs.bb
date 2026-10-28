# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Initramfs for chain-loading web888-ng from the stock firmware"
DESCRIPTION = "BusyBox and an init that mounts web888-image's squashfs \
(appended by scripts/chainboot-pack.sh) as the root. Used with \
hw/tools/chainboot of web888-ng-hw, under the armed hardware watchdog."
LICENSE = "MIT"

PACKAGE_INSTALL = "web888-chainboot-init busybox base-files base-passwd"

# no rootfs features in the initramfs
IMAGE_FEATURES = ""
PACKAGE_EXCLUDE = "kernel-image-*"
IMAGE_NAME_SUFFIX ?= ""
IMAGE_LINGUAS = ""

IMAGE_FSTYPES = "cpio.gz"
inherit core-image

IMAGE_ROOTFS_SIZE = "8192"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
