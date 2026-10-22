# SPDX-License-Identifier: GPL-3.0-or-later
# Boot partition (FAT: boot files, the bitstream for web888d), read-only.
do_install:append() {
	echo "/dev/mmcblk0p1       /boot                vfat       ro,nofail 0  0" >> ${D}${sysconfdir}/fstab
}
