# SPDX-License-Identifier: MIT
# For acl 2.4.0 (recipes-backports/attr), which declares acl_*_at
# functions of its own: tar 1.35's clash. The patch from OpenEmbedded-Core
# master (34991d0). Drop with the acl backport.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://0001-Avoid-acl_-prefix-for-functions.patch"
