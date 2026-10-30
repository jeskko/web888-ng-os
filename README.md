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
the `web888d` service waits for `/dev/zynqsdr`, loads the variant's
bitstream through the FPGA manager and serves HPSDR (port 1024).

Bitstreams: package `web888-bitstream` fetches the web888-ng-hw release
(CI-built, checked by SHA-256) into `/usr/share/web888`; new release =
bump the recipe version and the two checksums from its `SHA256SUMS`. To
try another bitstream, put it on the FAT boot partition (read-only at
`/boot`) and change `/etc/default/web888d` to `serve --bitstream
/boot/<file>.bit`. A configuration partition holds `web888d`'s
configuration.

The SD card image (`web888-image-web888.rootfs.wic.xz`) boots the board
directly: U-Boot SPL with our PS7 initialisation as `BOOT.BIN`, U-Boot
choosing root slot A or B (`meta-web888/files/wic/web888-sd.wks`), the
kernel and our device tree from the slot. The owner's settings go in
`web888.txt` on the card's FAT partition (`web888-setup`). Tested in the
emulator (web888-ng-hw `emu/tests/sd-image.sh`), not yet on a board:
`docs/testing-the-sd-image.md`. Tags publish it as a release.

Development image (empty root password, never for release):
`kas-container build kas/web888.yml:kas/dev.yml`. Boot it in the
emulator as the board does, from a copy of the image grown to 2 GB with
each slot's DTB given the emulator's changes (`emu/tests/sd-image.sh`
does both): `emu/run.sh --bootrom --boot-mode sd --sd <copy>`; it boots
straight into web888d. Why Yocto and the SD layout: `docs/os-base.md` in
web888-ng-hw.

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
