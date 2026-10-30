# SPDX-License-Identifier: GPL-3.0-or-later
# Boot partition (FAT "WEB888": the boot files and web888.txt),
# read-only; web888-setup remounts it for its few writes.
# Configuration partition (ext4, web888d's configuration and state):
# read-write, but only written when the configuration changes; checked at
# boot. By label: the root is either slot (root= from U-Boot).
do_install:append() {
	echo "LABEL=WEB888         /boot                vfat       ro,nofail 0  0" >> ${D}${sysconfdir}/fstab
	echo "LABEL=config         /config              ext4       rw,noatime,nofail 0  2" >> ${D}${sysconfdir}/fstab
	install -d ${D}/config
}

FILES:${PN} += "/config"
