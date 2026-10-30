# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "Web-888 owner's settings from web888.txt; A/B boot slot marked good"
DESCRIPTION = "web888-setup applies web888.txt from the SD card's FAT \
partition at boot (network, hostname, admin password, SSH key, a stable \
MAC without one in the EEPROM, the frp tunnel to a public VPS). web888-boot-ok gives this boot's root \
slot its tries back once web888d runs. Also: the U-Boot environment's \
place for fw_setenv, the watchdog fed by systemd, SSH host keys on the \
configuration partition."
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-3.0-or-later;md5=1c76c4cc354acaac30ed4d5eefea7245"

SRC_URI = " \
    file://web888-setup \
    file://web888-setup.service \
    file://web888-boot-ok \
    file://web888-boot-ok.service \
    file://web888.txt \
    file://fw_env.config \
    file://dropbear-web888.conf \
    file://dropbearkey-web888.conf \
    file://watchdog.conf \
"
S = "${UNPACKDIR}"

inherit systemd deploy

SYSTEMD_SERVICE:${PN} = "web888-setup.service web888-boot-ok.service"

do_install() {
	install -D -m 0755 ${S}/web888-setup ${D}${sbindir}/web888-setup
	install -D -m 0755 ${S}/web888-boot-ok ${D}${sbindir}/web888-boot-ok
	install -D -m 0644 ${S}/web888-setup.service ${D}${systemd_system_unitdir}/web888-setup.service
	install -D -m 0644 ${S}/web888-boot-ok.service ${D}${systemd_system_unitdir}/web888-boot-ok.service
	install -D -m 0644 ${S}/fw_env.config ${D}${sysconfdir}/fw_env.config
	install -D -m 0644 ${S}/dropbear-web888.conf ${D}${systemd_system_unitdir}/dropbear.socket.d/web888.conf
	install -D -m 0644 ${S}/dropbearkey-web888.conf ${D}${systemd_system_unitdir}/dropbearkey.service.d/web888.conf
	install -D -m 0644 ${S}/watchdog.conf ${D}${systemd_unitdir}/system.conf.d/web888-watchdog.conf
}

# the settings file as shipped on the FAT (IMAGE_BOOT_FILES); the
# channelizer image's with its bitstream setting on
do_deploy() {
	install -m 0644 ${S}/web888.txt ${DEPLOYDIR}/web888.txt
	sed 's/^#bitstream = standard\r$/bitstream = channelizer\r/' ${S}/web888.txt > ${DEPLOYDIR}/web888-channelizer.txt
	grep -q '^bitstream = channelizer' ${DEPLOYDIR}/web888-channelizer.txt
}
addtask deploy after do_install before do_build

FILES:${PN} += "${systemd_system_unitdir} ${systemd_unitdir}/system.conf.d"
PACKAGE_ARCH = "${MACHINE_ARCH}"

# web888d passwd, fw_printenv/fw_setenv, mount, od, sed; frp: the frpc
# that the frp_* settings configure
RDEPENDS:${PN} = "web888d libubootenv-bin frp"
