# Security

This repository holds the SD card image and its update bundles (RAUC) of web888-ng, an open replacement for
the Web-888 SDR's firmware. Releases are pre-releases, maintained on a
best-effort basis.

## Reporting a vulnerability

Please report it privately: the repository's **Security** tab, **Report
a vulnerability** (GitHub's private reporting). Don't open a public
issue for it. Say what is affected (release or commit), how to reproduce
it, and what it allows.

## Supported versions

Only the newest release and `main` get fixes. An installed unit takes
fixes by updating to the newest web888-ng-os release (from os v0.9.0,
from the admin page's **Update** tab).

## Dependencies

Dependabot watches this repository's GitHub Actions, and CodeQL scans
the workflows. The image's packages (the kernel, OpenSSL, curl,
Dropbear, RAUC and the rest) come from Yocto's layers, pinned in
`kas/web888.lock.yml`; Dependabot can't see them, so they are updated
with Yocto's point releases. web888d's own dependencies are watched in
web888-ng.
