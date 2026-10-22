# SPDX-License-Identifier: GPL-3.0-or-later
# Boot partition (FAT: boot files, the bitstream for web888d), read-only.
# Configuration partition (ext4, web888d's configuration and state):
# read-write, but only written when the configuration changes; checked at
# boot. The mount point lives on the read-only root.
do_install:append() {
	echo "/dev/mmcblk0p1       /boot                vfat       ro,nofail 0  0" >> ${D}${sysconfdir}/fstab
	echo "/dev/mmcblk0p3       /config              ext4       rw,noatime,nofail 0  2" >> ${D}${sysconfdir}/fstab
	install -d ${D}/config
}

FILES:${PN} += "/config"
