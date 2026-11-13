# SPDX-License-Identifier: GPL-3.0-or-later
# No containers or VMs on the unit: systemd-machined isn't wanted (and
# CVE-2026-40224, a privilege escalation through it, then doesn't apply)
PACKAGECONFIG:remove = "machined nss-mymachines"
CVE_STATUS[CVE-2026-40224] = "${@bb.utils.contains('PACKAGECONFIG', 'machined', 'unpatched', 'not-applicable-config: systemd-machined is not built', d)}"
