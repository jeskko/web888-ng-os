# SPDX-License-Identifier: GPL-3.0-or-later
# avahi-daemon answers <hostname>.local. Its simple-protocol socket
# (/run/avahi-daemon/socket) is local only, and root is the only account
# with a login.
CVE_STATUS[CVE-2025-59529] = "not-reachable: a local denial of service through avahi-daemon's local socket; root is the only account with a login"
# The scan of 2026-11-25 (fixed in 0.9-rc5, not released as 0.9 yet)
CVE_STATUS[CVE-2026-59987] = "not-reachable: a local D-Bus client's RecordBrowserNew call crashes avahi-daemon; root is the only account with a login"
CVE_STATUS[CVE-2026-58406] = "not-applicable-platform: a negative char passed to tolower(); char is unsigned on ARM (armhf), so the value can't be negative"
