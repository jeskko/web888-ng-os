# SPDX-License-Identifier: GPL-3.0-or-later
# the same tag from U-Boot's GitHub mirror, as u-boot_%.bbappend
SRC_URI:remove = "git://source.denx.de/u-boot/u-boot.git;protocol=https;branch=master;tag=v${PV}"
SRC_URI:prepend = "git://github.com/u-boot/u-boot.git;protocol=https;branch=master;tag=v${PV} "
