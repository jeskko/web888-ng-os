# SPDX-License-Identifier: GPL-3.0-or-later
# The scan of 2026-11-25: 1.5.7's decompressor can leak a 64 KiB internal
# buffer into its output (fixed in 1.6.0). In the image only mksquashfs
# and unsquashfs link libzstd: root-only tools, and rauc runs unsquashfs
# only on a bundle whose signature it has already checked (verity
# bundles only, system.conf); installs mount the bundle with the
# kernel's own squashfs. No one outside can hand it data and read the
# output back.
CVE_STATUS[CVE-2026-124851] = "not-reachable: libzstd is linked only by mksquashfs and unsquashfs (root-only; rauc runs unsquashfs only on bundles whose signature it has checked)"
