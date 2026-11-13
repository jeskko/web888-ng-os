# SPDX-License-Identifier: GPL-3.0-or-later
CVE_STATUS[CVE-2026-40224] = "${@bb.utils.contains('PACKAGECONFIG', 'machined', 'unpatched', 'not-applicable-config: systemd-machined is not built', d)}"
