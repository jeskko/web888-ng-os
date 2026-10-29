#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Assemble the A/B SD card layout from parts, without root (for the
# emulator and for trying parts before the Yocto build has them):
#
#   ab-sdimage.sh OUT ROOT.ext4 KERNEL DTB BOOT.BIN U-BOOT.IMG [WEB888.TXT]
#
#   0 - 4 MiB   MBR; U-Boot's environment at 1 MiB and 1.25 MiB (raw)
#   p1  64 MiB  FAT "BOOT": BOOT.BIN, u-boot.img, web888.txt
#   p2 512 MiB  ext4 slot A: the root, with /boot/<kernel> and the DTB
#   p3 512 MiB  ext4 slot B: the same at first
#   p4  32 MiB  ext4 "config": empty (web888d's configuration and state)
#
# ROOT.ext4 is a root file system image (e.g. partition 2 of the image's
# .wic). Its fstab is rewritten for this layout. KERNEL is a zImage or a
# uImage; it goes to /boot under its own kind's name.
set -eu
[ $# -ge 6 ] || { sed -n '3,15p' "$0" >&2; exit 2; }
out=$1 root=$2 kernel=$3 dtb=$4 bootbin=$5 uboot=$6 txt=${7:-}
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

MiB=2048 # sectors
p1=$((4 * MiB)) s1=$((64 * MiB))
p2=$((p1 + s1)) s2=$((512 * MiB))
p3=$((p2 + s2)) s3=$s2
p4=$((p3 + s3)) s4=$((32 * MiB))
total=$((p4 + s4))

rm -f "$out"
truncate -s $((total * 512)) "$out"
sfdisk -q "$out" <<EOF
label: dos
$p1,$s1,c,*
$p2,$s2,83
$p3,$s3,83
$p4,$s4,83
EOF

# p1: FAT with the boot files
mkfs.vfat -n BOOT -C "$tmp/boot.fat" $((s1 / 2)) > /dev/null
export MTOOLS_SKIP_CHECK=1
mcopy -i "$tmp/boot.fat" "$bootbin" ::BOOT.BIN
mcopy -i "$tmp/boot.fat" "$uboot" ::u-boot.img
[ -n "$txt" ] && mcopy -i "$tmp/boot.fat" "$txt" ::web888.txt
dd if="$tmp/boot.fat" of="$out" bs=512 seek=$p1 conv=notrunc status=none

# p2 and p3: the root, grown to the slot, with the kernel and the DTB
cp "$root" "$tmp/root.ext4"
e2fsck -fp "$tmp/root.ext4" > /dev/null || [ $? -le 1 ]
resize2fs -f "$tmp/root.ext4" $((s2 / 2))K > /dev/null 2>&1
case $(head -c 4 "$kernel" | od -An -tx1 | tr -d ' ') in
	27051956) kname=uImage ;;
	*) kname=zImage ;;
esac
cat > "$tmp/fstab" <<'EOF'
# A/B layout (ab-sdimage.sh): the root comes from the kernel command line
/dev/root            /                    auto       ro                    1  0
proc                 /proc                proc       defaults              0  0
devpts               /dev/pts             devpts     mode=0620,ptmxmode=0666,gid=5      0  0
tmpfs                /run                 tmpfs      mode=0755,nodev,nosuid,strictatime 0  0
tmpfs                /var/volatile        tmpfs      defaults              0  0
LABEL=BOOT           /boot                vfat       ro,nofail             0  0
LABEL=config         /config              ext4       rw,noatime,nofail     0  2
EOF
debugfs -w "$tmp/root.ext4" > /dev/null 2>&1 <<EOF
mkdir /boot
write $kernel /boot/$kname
write $dtb /boot/zynq-web888.dtb
rm /etc/fstab
write $tmp/fstab /etc/fstab
EOF
for p in $p2 $p3; do
	dd if="$tmp/root.ext4" of="$out" bs=512 seek=$p conv=notrunc status=none
done

# p4: the configuration partition, empty
mkfs.ext4 -q -L config "$tmp/config.ext4" $((s4 / 2))K
dd if="$tmp/config.ext4" of="$out" bs=512 seek=$p4 conv=notrunc status=none

echo "$out: A/B layout, $((total / MiB)) MiB, kernel /boot/$kname"
