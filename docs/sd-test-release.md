# First test release: an SD card image (draft)

Status: **draft for discussion**, 2026-10-29.

The goal is one SD card image that a Web-888 owner can write to a spare card,
boot, and use. They set the network and the admin password by editing a text
file on the card from any PC. The image boots our own boot loader and kernel
straight from the card, with no chain-loading through the stock firmware. To go
back to the stock firmware, the owner puts the original card back in.

## 1. What a tester does

1. Download `web888-ng-sd-<version>.img.xz` and check it against
   `SHA256SUMS`.
2. Write it to a **spare** microSD card (1 GB or more) with Raspberry Pi
   Imager, balenaEtcher or `dd`. Keep the original card as it is: it is the
   way back.
3. Put the card back in the PC. It shows a FAT drive, `WEB888`. Open
   `web888.txt` and set the admin password. Set a static address too if the
   network has no DHCP.
4. Put the card in the Web-888, connect Ethernet and the antenna, and power
   it on.
5. Open `http://web888.local:8073` or the address the router gave the unit,
   and log in to the admin pages with the password from step 3.

If anything fails, power off and put the original card back. Nothing on the
device is written except the SD card.

## 2. Boot chain: our own, straight from the card

The stock card's `boot.bin` holds the vendor's FSBL, a jump stub, the DTB,
the kernel and an initrd. There is no U-Boot (`hw/docs/stock-unit-facts.md`
§Boot chain). Ours:

| piece | what | from |
|---|---|---|
| `BOOT.BIN` | FSBL + U-Boot | os CI (meta-xilinx FSBL, or U-Boot SPL with `ps7_init_gpl`) |
| FSBL / SPL | PS7 init: DDR, MIO, clocks | our block design's PS7 configuration (web888-ng-hw), checked against the board |
| U-Boot | loads kernel and DT, passes the MAC | its env read-only from the I2C EEPROM (the board data, `ethaddr`), never saved |
| kernel + DT | linux-xlnx 6.18, **our DT for the real board** | `os/meta-web888`, from the live DT (`stock-unit-facts.md` §Device tree) |
| root | the image's root, read-only (ext4 now; squashfs A/B later) | as today |
| bitstream | the open HF bitstream (web888-ng-hw release) | `web888-bitstream` |

U-Boot is the planned base for A/B updates (`hw/docs/os-base.md`). Using it
now means testers' cards can later be updated in place.

What the stock FSBL does that we must do ourselves:

* **The Si5351** (the ADC clock): the stock FSBL programs it over I2C.
  Today our chain-loaded image inherits that setting. Ours sets it either in
  the DT, via the kernel's `clk-si5351` driver, or with a small boot-time
  step before `web888d`, and checks for 122.88 MHz with the PLL locked.
* **DDR and MIO settings**: from our block design's PS7 configuration. We
  check them against the stock FSBL's register writes (facts, not code) and
  run `memtester` on the first boards.
* **The MAC**: from the EEPROM board data, as U-Boot reads it. Without
  that, a random MAC would give a new DHCP address on every boot.

## 3. Configuration on the card: `web888.txt`

A plain `key = value` file on the FAT partition. Windows Notepad and macOS
TextEdit can edit it. Lines starting with `#` are comments. The image ships
it with every setting commented out and explained:

```
# Web-888 (web888-ng) settings. Saved changes apply at the next power-on.

# Admin password for the web admin pages (8 characters or more). After the
# unit has taken it, the line reads "admin_password = (set)" and the
# password is no longer on this card. Write a new one here to change it.
#admin_password =

# Network: dhcp (default) or static.
#network = dhcp
#address = 192.168.1.50/24
#gateway = 192.168.1.1
#dns = 192.168.1.1

# Name on the network: <hostname>.local. Default web888.
#hostname = web888

# Optional: an SSH public key for root (one line). No other way to log in.
#ssh_key =
```

At boot, a one-shot `web888-setup.service` (before networking and
`web888d`):

* **Network**: writes `/run/systemd/network/` from the file on every boot,
  so the file is always the truth and nothing persistent is needed. DHCP
  (default) or static. mDNS on (`systemd-resolved`) answers
  `<hostname>.local`. Without a DHCP lease the unit takes a link-local
  address, so a laptop connected straight to it can still reach it.
* **Admin password**: hashes it with `web888d passwd` into `/config`, then
  rewrites the line as `admin_password = (set)`. That is the only write to
  the FAT, once per change, done as write a copy then rename. This replaces
  today's `/boot/web888d-admin`, which leaves the password on the card.
* **No password given**: the unit makes a random one, writes it to
  `web888-password.txt` on the FAT, and sets it. The admin pages are never
  open, and there is no shared default like the MAC address.
* **SSH**: off unless `ssh_key` is set. There is no root password.

Bad lines are reported in `web888-setup.log` on the FAT: one short file,
rewritten at each boot, so a tester can see why a setting didn't take.

## 4. The release

* os CI builds the SD image from tags. The release carries:
  * `web888-ng-sd-<version>.img.xz` and `SHA256SUMS`;
  * the SBOM/SPDX and licence manifest;
  * a source note (the GPL: recipes, kernel config, layers at the tag).
* The image contains web888d (v0.4.0 or later) with its UI, and the open HF
  bitstream. The channelizer bitstream is an option in `web888.txt`
  (`bitstream = channelizer`), marked experimental, once it has run
  released.
* It's a GitHub **pre-release**. The notes say what works, what doesn't,
  how to go back, and what to report.
* Issue template: board `hw_rev` and serial (the admin page shows them from
  the EEPROM), image version, what was tried, and `web888-setup.log`.
* Size: the FAT partition shrinks from 512 MB to ~64 MB, so the image fits
  any card and writes fast.

## 5. Checks before the first tester

1. **Emulator** (`hw/emu`): the full SD image boots through our `BOOT.BIN`,
   `web888.txt` applies (DHCP, static, a password, a bad line), and the
   password line is rewritten.
2. **A real board with someone at it**: the user's own unit or the
   reordered test unit, never the remote production unit (its `BOOT.BIN`
   is not written, and nobody can swap its card). Check list:
   * DDR (`memtester` over most of RAM);
   * Ethernet, the MAC from the EEPROM, DHCP and static;
   * Si5351: ADC at 122.88 MHz with the PLL locked; a known signal at the
     right frequency;
   * PPS/GPS, the watchdog, temperature;
   * FT8 decodes compared with the stock firmware on the same antenna;
   * a 24 h soak;
   * power cuts during a password change.
3. **Two or three friendly testers**, then a wider call.

## 6. Work, in order

| # | work | where |
|---|---|---|
| 1 | DT for the real board; Si5351 set up from Linux; MAC from the EEPROM | os, hw docs |
| 2 | `BOOT.BIN` in the os build (FSBL + U-Boot, env read-only from the EEPROM) | os |
| 3 | `web888-setup` (the file, network, password, SSH key, log) + mDNS | os, server (`web888d passwd` from a file) |
| 4 | SD image: a smaller FAT, `web888.txt` shipped, release CI on tags | os CI |
| 5 | emulator tests of 2-4 | hw/emu |
| 6 | first boot on a real board with someone at it; the check list | user / test unit |
| 7 | release notes, issue template, pre-release | os |

## 7. Open questions

* The file's name and format: `web888.txt` with `key = value`, or TOML
  like web888d's own configuration (stricter, but easier to get wrong in
  Notepad).
* Should the default hostname carry part of the MAC (`web888-a1b2c3`) so
  two units on one network don't clash? A plain `web888` is easier to type.
* Which of the units testers have can we check before release? Are their
  board revisions and DDR parts the same as ours? The EEPROM `hw_rev` tells
  us after the fact.
* Wi-Fi: not on this board as far as we know; Ethernet only.
