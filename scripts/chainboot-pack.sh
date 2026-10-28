#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Package an image for chain-loading from the stock firmware
# (hw/tools/chainboot of web888-ng-hw):
#
#   chainboot-pack.sh OUT KERNEL ROOTFS INITRAMFS DTB [OVERLAY]
#
# KERNEL     uImage or zImage (CI artifact web888-image)
# ROOTFS     web888-image-web888.rootfs.squashfs-xz
# INITRAMFS  web888-chainboot-initramfs-web888.cpio.gz
# DTB        the unit's device tree (from its boot.bin: hw/emu/scripts/bootbin.py)
# OVERLAY    optional directory copied over the root at boot: the unit's
#            network, ssh keys, /config/web888d.toml (keep it private)
#
# Environment: MAC (eth0 address, e.g. the stock one, so the LAN sees the
# same host), BOOTARGS_EXTRA (e.g. netconsole=@/eth0,6666@HOST/).
#
# Writes OUT/zImage, OUT/unit.dtb and OUT/initrd.cpio.gz; on the unit:
#   chainboot.sh zImage unit.dtb initrd.cpio.gz [go]
set -eu
[ $# -ge 5 ] || { sed -n '3,22p' "$0" >&2; exit 2; }
out=$1 kernel=$2 rootfs=$3 initramfs=$4 dtb=$5 overlay=${6:-}
for t in fdtput cpio gzip; do
	command -v $t > /dev/null || { echo "needs $t" >&2; exit 1; }
done
mkdir -p "$out"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

# uImage = 64-byte header + zImage; the zImage magic is at 0x24
magic() { od -An -tx4 -j 36 -N 4 "$1" | tr -d ' '; }
if [ "$(magic "$kernel")" = 016f2818 ]; then
	cp "$kernel" "$out/zImage"
else
	dd if="$kernel" of="$out/zImage" bs=64 skip=1 status=none
	[ "$(magic "$out/zImage")" = 016f2818 ] || { echo "$kernel: not a zImage or uImage" >&2; exit 1; }
fi

# the initramfs, then the root and the overlay as a second archive
mkdir "$tmp/root"
cp "$rootfs" "$tmp/root/rootfs.squashfs"
if [ -n "$overlay" ]; then
	cp -a "$overlay" "$tmp/root/overlay"
fi
cp "$initramfs" "$out/initrd.cpio.gz"
(cd "$tmp/root" && find . | LC_ALL=C sort | cpio --quiet -o -H newc -R 0:0) | gzip -1 >> "$out/initrd.cpio.gz"
size=$(stat -c %s "$out/initrd.cpio.gz")

cp "$dtb" "$out/unit.dtb"
fdtput -t s "$out/unit.dtb" /chosen bootargs \
	"console=ttyPS0,115200 earlycon initrd=0x04000000,$size clk_ignore_unused chainboot=1${BOOTARGS_EXTRA:+ $BOOTARGS_EXTRA}"
if [ -n "${MAC:-}" ]; then
	fdtput -t bx "$out/unit.dtb" /axi/ethernet@e000b000 local-mac-address $(echo "$MAC" | tr : ' ')
fi
echo "$out: zImage $(stat -c %s "$out/zImage"), initrd $size bytes at 0x04000000"
fdtget "$out/unit.dtb" /chosen bootargs
