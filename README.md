# web888-ng-os: firmware image for the Web-888 SDR

Yocto 6.0 "Wrynose" image for the Web-888 HF receiver (Zynq-7010): the
`meta-web888` layer and a [kas](https://kas.readthedocs.io) configuration.
Part of web888-ng, together with
[web888-ng-hw](https://github.com/jeskko/web888-ng-hw) (open FPGA, docs,
QEMU virtual Web-888) and [web888-ng](https://github.com/jeskko/web888-ng)
(the `web888d` server).

Status: **bring-up**. The image builds (linux-xlnx 6.18 LTS, systemd,
~42 MB squashfs) and boots in the QEMU virtual Web-888 of web888-ng-hw
with a read-only root. The open `web888-sdr` driver loads automatically;
the `web888d` service loads the bitstream from the boot partition through
the FPGA manager and serves HPSDR (port 1024).

The bitstream is not in the image yet: copy an open bitstream
(web888-ng-hw `fpga/build/hf/websdr_hf.bit`) to the FAT boot partition
as `web888-pl.bit` (mounted read-only at `/boot`; command line in
`/etc/default/web888d`). Planned: bitstreams built by CI and fetched by a
recipe from web888-ng-hw releases. Next also: our own device tree, RAUC
A/B with the config partition, and the boot files for the real board.

Development image (empty root password, never for release):
`kas-container build kas/web888.yml:kas/dev.yml`. Boot it in the
emulator with `emu/run.sh --kernel <zImage> --initrd none --sd
<image>.wic.qemu-sd --append "console=ttyPS0,115200 root=/dev/mmcblk0p2
rootwait ro"`; put the bitstream into a copy of the image first
(`mcopy -i <copy>@@4096 websdr_hf.bit ::web888-pl.bit`). Without a
bitstream, add `web888_sdr.pl_check=0` to use the open driver directly
(the PL readiness check would refuse). Why Yocto and the planned SD
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

With rootless podman, `kas-container`'s user switching can fail; use
`KAS_WORK=<build dir> scripts/kas-podman.sh build kas/web888.yml` from a
plain clone instead.

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
