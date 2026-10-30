# web888-ng-os: firmware image for the Web-888 SDR

Yocto 6.0 "Wrynose" image for the Web-888 HF receiver (Zynq-7010): the
`meta-web888` layer and a [kas](https://kas.readthedocs.io) configuration.
Part of web888-ng, together with
[web888-ng-hw](https://github.com/jeskko/web888-ng-hw) (open FPGA, docs,
QEMU virtual Web-888) and [web888-ng](https://github.com/jeskko/web888-ng)
(the `web888d` server).

Status: **released for testers** (pre-releases on the Releases page).
v0.2.0's SD card image has booted on a Web-888 from the card (a tester's
unit, 2026-10-30), with the right RF frequencies; most testing is still
in the emulator of web888-ng-hw. Releases so far: v0.1.0 (the first SD
card image), v0.2.0 (a second image with the channelizer bitstream) and
v0.3.0 (updates over the network, tunnels, web888d v0.6.0).

What the image is:

* **Boot:** U-Boot SPL with our PS7 initialisation as `BOOT.BIN`, then
  U-Boot, which sets the ADC clock (Si5351, 122.88 MHz) and picks root
  slot A or B with a boot counter
  (`meta-web888/recipes-bsp/u-boot/files/web888.env`). The kernel and
  device tree come from the slot.
* **Card layout** (`meta-web888/files/wic/web888-sd.wks`):
  * the FAT `WEB888` (boot files and `web888.txt`);
  * two 512 MB ext4 root slots;
  * an ext4 configuration partition.
* **System:** linux-xlnx 6.18 LTS, systemd, a read-only root.
  * The open `web888-sdr` driver and web888d, which loads the bitstream
    through the FPGA manager and serves the web UI (8073) and HPSDR
    (1024).
  * Bitstreams come from web888-ng-hw releases (`web888-bitstream`,
    checked by SHA-256): HF and VHF from v0.2.0, the channelizer from
    v0.3.0.
* **Owner settings** go in `web888.txt` on the FAT (`web888-setup`):
  * network, hostname, admin password, an SSH key and the bitstream;
  * optional tunnels to the Internet (frp, Cloudflare Tunnel, rathole or
    chisel): `docs/tunnels.md`.
  * `web888-setup.log` next to the file says what was applied.
* **Updates (from v0.3.0):** signed RAUC bundles, installed over the
  network into the other slot with `rauc install <URL>`. The unit falls
  back to the old slot if the new one doesn't come up: `docs/updates.md`.
* **For testers:** `docs/testing-the-sd-image.md`.

Chain-loading from the stock firmware (no card change):
`web888-chainboot-initramfs` with web888-ng-hw's `tools/chainboot`. The
project's own remote unit runs this way.

Development image (empty root password, never for release):
`kas-container build kas/web888.yml:kas/dev.yml`. Boot it in the
emulator as the board does, from a copy of the image grown to 2 GB with
each slot's DTB given the emulator's changes:
`emu/run.sh --bootrom --boot-mode sd --sd <copy>`. `emu/tests/sd-image.sh`
does both and checks the boot; `emu/tests/ota.sh` checks updates. Why
Yocto and the SD layout: `docs/os-base.md` in web888-ng-hw.

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

Output lands in `build/tmp/deploy/images/web888/`. The update bundle
needs a signing key (`docs/updates.md`):
`WEB888_RAUC_KEY=<key.pem> kas build kas/web888.yml:kas/bundle.yml`.

## CI

`config` (GitHub-hosted, also for pull requests) checks that the kas
configuration parses. `image` builds the full image on the project's
self-hosted runner, only for pushes to `main` and tags, never for pull
requests. It is enabled by the repository variable `SELF_HOSTED_RUNNER`,
and signs the update bundle with the secret `WEB888_RAUC_KEY`. A `v*` tag
publishes a GitHub release with the card images, the update bundle, their
SBOM and `SHA256SUMS`, and the notes from `docs/release-notes/<tag>.md`.

## Licence

GPL-3.0-or-later (`COPYING`). Recipes for third-party software keep their
upstream licences; the image's licence manifest and SPDX documents list
them.
