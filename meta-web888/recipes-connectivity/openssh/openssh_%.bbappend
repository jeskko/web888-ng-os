# SPDX-License-Identifier: GPL-3.0-or-later
# The image takes only openssh-sftp-server from this recipe (recommended
# by packagegroup-core-ssh-dropbear, so that scp from current OpenSSH
# clients, which speaks SFTP, works); the server is dropbear. These are
# flaws in sshd or the ssh client, neither of which is installed.
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_OPENSSH"
CVE_STATUS_WEB888_OPENSSH = "CVE-2026-106553 CVE-2026-106555 CVE-2026-106583"
CVE_STATUS_WEB888_OPENSSH[status] = "not-applicable-config: in sshd or ssh; the image installs only openssh-sftp-server (the SSH server is dropbear)"
