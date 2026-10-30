# SPDX-License-Identifier: GPL-3.0-or-later
# The same image, shipped with the channelizer bitstream chosen in
# web888.txt (bitstream = channelizer; standard switches back).
require web888-image.bb
SUMMARY = "Web-888 SDR firmware image, channelizer bitstream"

IMAGE_BOOT_FILES = "boot.bin u-boot.img web888-channelizer.txt;web888.txt"
