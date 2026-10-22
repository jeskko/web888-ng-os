# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Web-888 SDR firmware image"
LICENSE = "MIT"

inherit core-image

# Read-only root; the configuration partition is mounted separately
# (read-write only while saving). RAUC A/B comes next.
IMAGE_FEATURES += "read-only-rootfs ssh-server-dropbear"

IMAGE_LINGUAS = ""
IMAGE_FSTYPES += "squashfs-xz"
