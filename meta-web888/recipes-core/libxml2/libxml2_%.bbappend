# SPDX-License-Identifier: GPL-3.0-or-later
# In the image only update-mime-database links libxml2. It reads the
# shared-mime-info package's own XML when packages are installed; no
# service on the unit parses XML from outside with it.
CVE_STATUS_WEB888_LIBXML2 = "CVE-2026-137067 CVE-2026-137068 CVE-2026-137069 CVE-2026-137072 CVE-2026-137073 CVE-2026-137074 CVE-2026-137075 CVE-2026-137082"
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_LIBXML2"
CVE_STATUS_WEB888_LIBXML2[status] = "not-reachable: only update-mime-database links libxml2, on the shared-mime-info package's own files"
