# SPDX-License-Identifier: GPL-3.0-or-later
# Fixed in the release/2.43/master commits this glibc is built from (the
# distro's SRCREV_glibc); the CVE databases only see "2.43+git".
CVE_STATUS[CVE-2026-4046] = "backported-patch: release/2.43/master 8362e8ce (IBM1390/IBM1399 pending character state)"
CVE_STATUS[CVE-2026-4437] = "backported-patch: release/2.43/master 5c6fca0c (resolv: count records correctly)"
CVE_STATUS[CVE-2026-5435] = "backported-patch: release/2.43/master e64ae5a5 (resolv: more types as unknown in ns_sprintrrf)"
CVE_STATUS[CVE-2026-5450] = "backported-patch: release/2.43/master 4070d808 (scanf %mc buffer overflow, BZ #34008)"
CVE_STATUS[CVE-2026-5928] = "backported-patch: release/2.43/master 2890b35c (ungetwc on a byte stream, BZ #33998)"
CVE_STATUS[CVE-2026-19499] = "backported-patch: release/2.43/master 713998bf (strfmon right-justification, bug 34510)"
CVE_STATUS[CVE-2026-84243] = "backported-patch: release/2.43/master 45e49b1e (intl: LANGUAGE path traversal, BZ #17142)"
CVE_STATUS[CVE-2026-100290] = "backported-patch: release/2.43/master e6ae66ce (regcomp/regexec double free after a failed allocation, BZ 34991)"
CVE_STATUS[CVE-2026-115819] = "backported-patch: release/2.43/master 124e9adc (wide scanf %s and %[ double null terminator, BZ 34989)"
CVE_STATUS[CVE-2026-126765] = "backported-patch: release/2.43/master 95a049c9 (glob double free after a failed allocation, BZ 34993)"
CVE_STATUS[CVE-2026-121758] = "backported-patch: release/2.43/master 5f7cc102 (ISO-2022-CN-EXT SO without a designation, bug 34774)"
# No program in the image calls this (its dynamic imports, 2026-11-17)
CVE_STATUS[CVE-2010-4756] = "not-applicable-config: glob() with an attacker's pattern (an FTP daemon's STAT); no program in the image calls glob()"
