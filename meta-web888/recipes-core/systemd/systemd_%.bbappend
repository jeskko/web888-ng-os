# SPDX-License-Identifier: GPL-3.0-or-later
# No containers or VMs on the unit: systemd-machined isn't wanted (and
# CVE-2026-40224, a privilege escalation through it, then doesn't apply)
PACKAGECONFIG:remove = "machined nss-mymachines"
CVE_STATUS[CVE-2026-40224] = "not-applicable-config: systemd-machined is not built (PACKAGECONFIG:remove above)"
