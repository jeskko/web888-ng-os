# SPDX-License-Identifier: GPL-3.0-or-later
# OpenSSL 3.5.9 flaws without a fixed 3.5 release yet (2026-11-17; drop
# these when oe-core's wrynose has one). In the image libcrypto/libssl
# are used by rauc (bundle signatures: CMS verify, and decrypting), kmod
# (module signatures), libcurl (rauc's HTTPS; built without HTTP/3, so no
# QUIC) and the openssl tool (root only). web888d has its own TLS
# (rustls/ring) and dropbear its own crypto.
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_OPENSSL_GCMSIV CVE_STATUS_WEB888_OPENSSL_QUIC CVE_STATUS_WEB888_OPENSSL_CMS"
CVE_STATUS_WEB888_OPENSSL_GCMSIV = "CVE-2026-88823 CVE-2026-95692"
CVE_STATUS_WEB888_OPENSSL_GCMSIV[status] = "ignored: AES-GCM-SIV, which nothing in the image uses (only the root-only openssl tool could be told to)"
CVE_STATUS_WEB888_OPENSSL_QUIC = "CVE-2026-95691 CVE-2026-88822 CVE-2026-124709 CVE-2026-107494 CVE-2026-84785"
CVE_STATUS_WEB888_OPENSSL_QUIC[status] = "ignored: OpenSSL QUIC (client and server), which nothing in the image uses (curl is built without HTTP/3: no ngtcp2/nghttp3, no openssl-quic)"
CVE_STATUS_WEB888_OPENSSL_CMS = "CVE-2026-107496"
CVE_STATUS_WEB888_OPENSSL_CMS[status] = "ignored: CMS_encrypt with an attacker's recipient certificate; on the device rauc only verifies and decrypts bundles (rauc encrypt runs on the build host with the operator's certificates), and the openssl tool is root only"
CVE_STATUS[CVE-2026-35190] = "ignored: a TLS 1.3 server with the Suite B certificate flag; nothing in the image runs an OpenSSL TLS server (libcurl and the openssl tool are clients here) or sets the flag"
CVE_STATUS[CVE-2026-107493] = "ignored: only when the caller passes its own stack to PEM_X509_INFO_read_bio_ex(); the image's callers (libcurl, the openssl tool) pass NULL"
# The scan of 2026-11-20 (OpenSSL 3.5.9, no fixed 3.5 release yet)
CVE_STATUS[CVE-2026-95690] = "ignored: DTLS handshake reassembly; nothing in the image speaks DTLS (libcurl and rauc use TLS over TCP)"
CVE_STATUS[CVE-2026-84786] = "ignored: a DANE-enabled TLS client; nothing in the image enables DANE (SSL_dane_enable): libcurl doesn't, and the openssl tool is root only"
CVE_STATUS[CVE-2026-121482] = "ignored: CMS/PKCS#7 verification with CRL checking enabled; rauc verifies bundles without CRL checks (no check-crl in system.conf), kmod only reads module signatures (the kernel verifies them), and the openssl tool is root only"
