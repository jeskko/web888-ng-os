# SPDX-License-Identifier: MIT
# Backported from OpenEmbedded-Core master (34991d0, 2026-11-09) for CVE
# fixes that wrynose doesn't have yet (licence lines in wrynose's "&"
# syntax), then moved to 2.11.0 (2026-11-25: CVE-2026-124317,
# CVE-2026-128241, CVE-2026-134703; the tarball's signature checked
# against Sebastian Pipping's key 3176EF7D...9AF37285). Drop it when
# wrynose catches up.

SUMMARY = "A stream-oriented XML parser library"
DESCRIPTION = "Expat is an XML parser library written in C. It is a stream-oriented parser in which an application registers handlers for things the parser might find in the XML document (like start tags)"
HOMEPAGE = "https://github.com/libexpat/libexpat"
SECTION = "libs"
LICENSE = "MIT"

LIC_FILES_CHKSUM = "file://COPYING;md5=f4fedd6116da0e171f7cb4d2923d7ac2"

VERSION_TAG = "${@d.getVar('PV').replace('.', '_')}"

SRC_URI = "${GITHUB_BASE_URI}/download/R_${VERSION_TAG}/expat-${PV}.tar.xz  \
           file://run-ptest \
           "

GITHUB_BASE_URI = "https://github.com/libexpat/libexpat/releases/"
UPSTREAM_CHECK_REGEX = "releases/tag/R_(?P<pver>.+)"

SRC_URI[sha256sum] = "61e7614b602201c28a189dafee78f5bceaee192bbde46a930752ff09cf5542fa"

EXTRA_OECMAKE:class-native += "-DEXPAT_BUILD_DOCS=OFF"

RDEPENDS:${PN}-ptest += "bash"

inherit cmake lib_package ptest github-releases

do_install_ptest:class-target() {
	install -m 755 ${B}/tests/runtests* ${D}${PTEST_PATH}
	install -m 755 ${B}/tests/benchmark/benchmark ${D}${PTEST_PATH}
}

BBCLASSEXTEND += "native nativesdk"

CVE_PRODUCT = "expat libexpat"


# In the image only dbus-daemon (and its launch helper) and avahi-daemon
# link libexpat, and they parse only their own root-owned configuration
# and service files with it: no XML from outside reaches the parser.
CVE_STATUS[CVE-2026-137951] = "not-applicable-config: needs XML_GE=0 with XML_UNICODE; built with the defaults (XML_GE=1, char)"
CVE_STATUS[CVE-2026-137992] = "not-reachable: duplicate ATTLIST declarations in an internal DTD subset; libexpat parses only dbus's and avahi's root-owned config files here (upstream fix PR #1537 not merged yet)"
