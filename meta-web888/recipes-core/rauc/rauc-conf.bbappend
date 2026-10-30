# SPDX-License-Identifier: GPL-3.0-or-later
# The Web-888's slots and the project's update CA (bundles signed by
# CI's release key, recipes-core/bundles)
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
RAUC_KEYRING_FILE = "web888-ca.cert.pem"
