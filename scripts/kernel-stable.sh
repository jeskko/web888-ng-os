#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Makes meta-web888/recipes-kernel/linux/linux-xlnx/linux-<STABLE>-stable.patch.xz:
# the Linux stable updates merged into Xilinx's kernel, as one commit.
#
#   scripts/kernel-stable.sh XILINX_TAG STABLE_TAG WORKDIR
#   e.g. scripts/kernel-stable.sh xlnx_rebase_v6.18_LTS_2026.2 v6.18.63 /data/kt
#
# Xilinx's tag must contain an earlier stable release (Xilinx merges them
# in). Files where Xilinx carries its own version and the merge conflicts
# keep Xilinx's version; they are listed in the patch's message, and
# CVEs fixed only in them want a CVE_STATUS in the recipe. WORKDIR wants
# a few GB; an existing linux-xlnx clone can lend its objects through
# REFERENCE=<path to its objects dir>.
set -eu
[ $# -eq 3 ] || { sed -n '3,14p' "$0"; exit 2; }
xtag=$1 stag=$2 work=$3
out=$(cd "$(dirname "$0")/.." && pwd)/meta-web888/recipes-kernel/linux/linux-xlnx
ver=${stag#v}
g() { git -c user.name=web888-ng -c user.email=web888-ng@users.noreply.github.com "$@"; }

[ -d "$work/.git" ] || git init -q "$work"
cd "$work"
[ -z "${REFERENCE:-}" ] || echo "$REFERENCE" > .git/objects/info/alternates
git fetch -q https://github.com/Xilinx/linux-xlnx "refs/tags/$xtag:refs/tags/$xtag"
# kernel.org's stable tree (its GitHub mirror answers the fetch better)
git fetch -q https://github.com/gregkh/linux "refs/tags/$stag:refs/tags/$stag"

g checkout -q -B web888-merge "$xtag"
if ! g merge -q --no-edit "$stag"; then
	conflicts=$(git diff --name-only --diff-filter=U)
	git checkout --ours -- $conflicts
	git add -- $conflicts
	g commit -q --no-edit
else
	conflicts=
fi

g checkout -q -B web888-stable "$xtag"
git diff "$xtag" web888-merge | git apply --index
{
	echo "Linux $ver: the stable updates since Xilinx's $xtag"
	echo
	echo "Xilinx linux-xlnx $xtag merged with stable $stag."
	echo "Where Xilinx carries its own version of a file and the merge"
	echo "conflicted, Xilinx's file is kept as it is (the stable changes to"
	echo "it are left out):"
	echo
	for f in $conflicts; do echo "  $f"; done
	echo
	echo "Made by os scripts/kernel-stable.sh."
	echo
	echo "Upstream-Status: Backport [stable $stag, merged into Xilinx's $xtag]"
} | g commit -q -F -
git diff --quiet web888-stable web888-merge

rm -f 0001-*.patch
git format-patch -q -1 --no-signature web888-stable
mkdir -p "$out"
xz -9 -T0 -c 0001-*.patch > "$out/linux-$ver-stable.patch.xz"
rm -f 0001-*.patch
echo "$out/linux-$ver-stable.patch.xz"
