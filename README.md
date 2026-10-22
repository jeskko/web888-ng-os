# web888-ng-os: firmware image for the Web-888 SDR

Yocto 6.0 "Wrynose" image for the Web-888 HF receiver (Zynq-7010): the
`meta-web888` layer and a [kas](https://kas.readthedocs.io) configuration.
Part of web888-ng, together with
[web888-ng-hw](https://github.com/jeskko/web888-ng-hw) (open FPGA, docs,
QEMU virtual Web-888) and [web888-ng](https://github.com/jeskko/web888-ng)
(the `web888d` server).

Status: **bring-up**. The image builds on a generic Zynq-7000 machine. Our
device tree, kernel (6.18 LTS with `fpga_manager`), RAUC A/B updates and
the boot files for the real board are next. Why Yocto and the planned SD
layout: `docs/os-base.md` in web888-ng-hw.

## Goals

* SD card read-only in normal operation; written only on configuration
  save (atomic, power-safe) and on updates.
* A/B updates with automatic rollback (RAUC + U-Boot boot counter).
* Small footprint; no package manager on the device.
* SBOM (SPDX) and CVE reports with every build.

## Build

Needs podman or docker; the build runs in the kas container. Plan for
~60 GB disk and a few hours for the first build:

```
kas-container build kas/web888.yml
```

Output lands in `build/tmp/deploy/images/web888/`.

## CI

`config` (GitHub-hosted, also for pull requests) checks that the kas
configuration parses. `image` builds the full image on the project's
self-hosted runner, only for pushes to `main` and tags, never for pull
requests. It is enabled by the repository variable `SELF_HOSTED_RUNNER`.

## Licence

GPL-3.0-or-later (`COPYING`). Recipes for third-party software keep their
upstream licences; the image's licence manifest and SPDX documents list
them.
