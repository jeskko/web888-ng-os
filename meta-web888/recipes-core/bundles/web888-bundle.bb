# SPDX-License-Identifier: GPL-3.0-or-later
SUMMARY = "web888-ng update bundle"
DESCRIPTION = "The root file system of web888-image (with the kernel, the \
device tree, the bitstreams and web888d) as a signed RAUC bundle, for \
'rauc install' on a unit running the SD card image. The channelizer \
image has the same root: one bundle for both."
LICENSE = "MIT"

inherit bundle

RAUC_BUNDLE_COMPATIBLE = "web888"
RAUC_BUNDLE_VERSION = "${DISTRO_VERSION}"
RAUC_BUNDLE_DESCRIPTION = "${DISTRO_NAME} ${DISTRO_VERSION}"
RAUC_BUNDLE_FORMAT = "verity"
RAUC_BUNDLE_SLOTS = "rootfs"
RAUC_SLOT_rootfs = "web888-image"
RAUC_SLOT_rootfs[fstype] = "ext4"

# Signed with the release key (kas/bundle.yml: WEB888_RAUC_KEY, a path;
# in CI a secret); its certificate, issued by the CA in the image's
# keyring (recipes-core/rauc), is here.
RAUC_KEY_FILE = "${WEB888_RAUC_KEY}"
RAUC_CERT_FILE = "${THISDIR}/files/release-1.cert.pem"

python () {
    if not d.getVar('WEB888_RAUC_KEY'):
        raise bb.parse.SkipRecipe("no signing key: set WEB888_RAUC_KEY (kas/bundle.yml)")
}
