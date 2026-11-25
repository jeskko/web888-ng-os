# SPDX-License-Identifier: GPL-3.0-or-later
# Crashes from a crafted awk program or shell script (no fix upstream yet,
# 2026-11-13). Nothing on the unit hands outside input to busybox awk or
# its shell: only root can log in and run commands, the image's own
# scripts don't use awk, and the one input they read is web888.txt on
# the card.
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_BUSYBOX"
CVE_STATUS_WEB888_BUSYBOX = "CVE-2026-38752 CVE-2026-38753 CVE-2026-38755"
CVE_STATUS_WEB888_BUSYBOX[status] = "not-reachable: needs a crafted awk program or shell script run on the unit; only root can run commands and no service passes outside input to awk or the shell"
# Symlink traversal when extracting an archive (tar, unzip, cpio):
# nothing on the unit extracts archives (RAUC bundles are signed
# SquashFS), only root could by hand
CVE_STATUS[CVE-2026-92205] = "not-reachable: needs a crafted archive extracted on the unit; no service extracts archives, and only root can run commands"
# The scan of 2026-11-25: path traversal in the FTP and TFTP servers.
# Neither applet is built (the image's busybox has only the tftp client;
# no ftpd or tftpd link, none in the binary's applet list).
CVE_STATUS[CVE-2026-136411] = "not-applicable-config: tftpd, which this busybox doesn't build (only the tftp client)"
CVE_STATUS[CVE-2026-136442] = "not-applicable-config: ftpd, which this busybox doesn't build"
