# Updates over the network (RAUC)

From v0.3.0 on, a unit running the SD card image can update itself
without taking the card out. Each release has an update bundle,
`web888-ng-<version>.raucb`, next to the card images. The bundle holds
the whole system: kernel, device tree, bitstreams, web888d and the rest.
One bundle serves both the standard and the channelizer image, since
they differ only in `web888.txt`.

Units on v0.1.0 or v0.2.0 have no RAUC: write the v0.3.0 card image
once, and update over the network from then on.

## Updating

Over SSH (an `ssh_key` in `web888.txt`):

```sh
rauc install https://github.com/jeskko/web888-ng-os/releases/download/v0.3.0/web888-ng-v0.3.0.raucb
reboot
```

* **Streamed:** RAUC reads the bundle straight from the URL, checking it
  as it goes, and writes it to the root slot not running now. Nothing is
  downloaded to the unit first. The receiver keeps running during the
  install.
* **Checked before it boots:** the bundle must be signed by the
  project's update key, and made for this hardware (`compatible=web888`).
  RAUC refuses anything else.
* **The new slot boots first,** with three tries. If web888d runs for a
  minute, the slot is marked good (`web888-boot-ok`). If the new system
  doesn't come up, the unit goes back to the old slot after three boots.
  The watchdog resets a hang.
* **Settings stay:** `web888.txt`, the admin password, the configuration
  partition and the SSH host keys are untouched by an update.

`rauc status` shows both slots, which one booted and which boots next.
A power cut during an install leaves the old slot as it was. The unit
boots it, and the install can simply be run again.

## What an update doesn't change

* `BOOT.BIN` and `u-boot.img` on the card's FAT partition. They aren't
  A/B, so a mistake there could leave the unit unbootable, and an update
  leaves them alone. A release that needs new ones will say so, and they
  are then copied to the card by hand.
* The U-Boot environment (the boot chooser's state) carries over.

## Signing

Bundles are signed in CI with the release key (a repository secret). Its
certificate is issued by the project's update CA, whose certificate is
the unit's keyring (`/usr/lib/rauc/web888-ca.cert.pem`). Only
certificates made for code signing are accepted (`check-purpose =
codesign`). The certificate is checked at the bundle's signing time, so
a unit whose clock isn't set yet can still install.

To run your own builds on a unit, build the image with your own CA in
`meta-web888/recipes-core/rauc/files/` and sign with your own key
(`WEB888_RAUC_KEY=<key> kas build kas/web888.yml:kas/bundle.yml`, with
your certificate in `recipes-core/bundles/files/`). A unit accepts
bundles only from the CA its running image has.

## Tested

In the emulator (web888-ng-hw `emu/tests/ota.sh`, with the SD card image
booted through the boot ROM as on the board):

* streaming install into slot B, then a boot into B, marked good;
* a power cut in the middle of an install;
* a bad update that doesn't boot, with the unit going back to the old
  slot.
