# Running a Web-888 on web888-ng

For an owner who wants to run a Web-888 on web888-ng day to day: putting
it on the card, the settings, updates, what the unit talks to on the
network, and what to do when it doesn't come up. (Testing a new release
on a board: [testing-the-sd-image.md](testing-the-sd-image.md).)

web888-ng replaces the Web-888's whole firmware: the boot loader, the
Linux system, the FPGA bitstream and the receiver's server (`web888d`).
It all runs from the SD card. Nothing on the unit itself is written, so
the original card brings the stock firmware back exactly as it was.

## 1. Install

You need the Web-888 with Ethernet and an antenna, a **spare** microSD
card of 2 GB or more (keep the original card as it is), and a computer
that writes SD cards (Raspberry Pi Imager, balenaEtcher, `bmaptool` or
`dd`).

1. From the newest release of
   [web888-ng-os](https://github.com/jeskko/web888-ng-os/releases),
   download `web888-ng-sd-<version>.img.xz` and `SHA256SUMS`. Check the
   file: `sha256sum -c SHA256SUMS --ignore-missing`.
2. Write the image to the spare card (the tools take the `.xz` as it
   is). With `dd`:
   `xz -dc web888-ng-sd-<version>.img.xz | sudo dd of=/dev/sdX bs=4M conv=fsync`
   (`/dev/sdX` is the card: check with `lsblk` first).
3. Take the card out and put it back in. A drive called `WEB888` appears.
   Open `web888.txt` in a text editor. Each setting is explained there;
   remove the `#` in front of the ones you use. Usually:
   * `admin_password`: the admin pages' password (8 characters or more).
     Without one, the unit makes one and writes it to
     `web888-password.txt` on the card.
   * `network = static` with `address`, `gateway` and `dns`, if there is
     no DHCP server (the default is DHCP).
   * `ssh_key`: your SSH public key, for a shell (`ssh root@<unit>`);
     one `ssh_key` line per key. Without a key, SSH is off.
   * Tunnels, to reach the receiver from the Internet without opening a
     port on your router: [tunnels.md](tunnels.md).
4. Save, eject, power the Web-888 off, put the card in, power on.
5. After about half a minute, open `http://web888.local:8073` (or the
   address your router gave the unit). The admin pages are under
   `#/admin` (the gear icon).
6. On the admin page, set the station: **Configuration**, `station`
   (callsign, locator, the receiver's name and place), then **Backup**,
   **Download the settings** (§4).

`web888-setup.log` on the card says, after each boot, what was taken from
`web888.txt` and any problem with a setting.

### Coming from the stock firmware

Nothing is carried over from the stock firmware's settings: set the
station, passwords and the rest again on the admin page. The unit uses
the MAC address in its EEPROM, so a DHCP reservation on your router still
applies (a board whose EEPROM has none gets one made at the first boot
and kept; `web888-setup.log` says which).

To go back to the stock firmware: power off and put the original card
back. Your web888-ng card keeps its settings for the next time.

## 2. Where the settings are

| what | where | changed by |
|---|---|---|
| network, hostname, SSH key, tunnels, safe mode | `web888.txt` on the card's FAT | editing the file on a computer; applied at the next boot |
| the receiver: station, bands, limits, skimmers, DX spots, metrics, the bitstream, ... | `web888d.toml` on the card's `config` partition | the admin page's **Configuration** (most apply at once; some need a web888d restart, the page says which) |
| passwords | the admin password's hash in `web888d.toml` | **Passwords**, or `admin_password` in `web888.txt` |
| the station's own frequency labels | `markers.csv` next to `web888d.toml` | **Labels** |

The bitstream: the channelizer (the default: the whole band in bins, 32
receivers, zoomed waterfalls from the FPGA) or the standard one (12
receivers, the one with the most hours on boards). Set it on the admin
page (Configuration, `radio.bitstream`: `auto`, `channelizer` or
`standard`). A `bitstream =` line in `web888.txt` overrides the page
(the page then shows the running value beside its own); remove the line
to let the page decide.

Secrets in `web888.txt` (the admin password, tunnel tokens) are taken
into the `config` partition at boot and their lines then read `(set)`,
so they don't stay readable on the card.

## 3. Updates

**The system** (web888d, the kernel, the bitstreams): **Update** on the
admin page looks for a new release once a day and installs it into the
second system slot while the receiver keeps running; **Reboot now**
starts it. If the new system doesn't come up, the unit goes back to the
old one by itself. Settings stay. Details and the SSH way:
[updates.md](updates.md).

**The boot loader** on the card is separate, because it has no second
copy to fall back to. Some releases bring a new one; their notes say so.
**Update**, **Boot loader** writes it when you ask. Before you do,
download a settings backup and have a card reader at hand: if the power
fails at the wrong moment, or the new boot loader doesn't start on your
board, the card has to be fixed on a computer or written again (§6). No
need to write it otherwise.

## 4. Backups

**Configuration**, **Backup**, **Download the settings** saves one file
(`web888d-settings-<date>.json`) with the configuration, the passwords
(as hashes) and the own labels. **Restore from a file** puts them back,
on this unit or on a newly written card. Keep the file private: anyone
with it can try to guess the passwords offline.

The backup doesn't have `web888.txt`'s settings: keep a copy of your
`web888.txt` (with the secrets written out, since the card's copy reads
`(set)` once taken), or write them in again after re-imaging. The SSH
host keys are made anew on a new card (your SSH client then warns once).

## 5. What the unit connects to

Out to the Internet, each only when its setting is on:

| to | why | setting (default) |
|---|---|---|
| `api.github.com`, `github.com` and its download host | looking for and installing releases | `update.enabled` (on), `update.check_hours` (24) |
| `spothole.app` (HTTPS) | live DX cluster spots shown on the spectrum | `dxspots.enabled` (on) |
| `report.pskreporter.info` (UDP 4739) | uploading FT8/FT4 spots from the skimmers | `report.pskreporter` (off; needs `station.callsign` and `station.locator`) |
| `wsprnet.org` (HTTP) | uploading WSPR spots | `report.wsprnet` (off; the same) |
| `time1-4.google.com` (NTP) | the clock (chrony; the time-code stations web888d receives also feed it) | always |
| your tunnel's server, or Cloudflare | the receiver from the Internet | a tunnel in `web888.txt` (none) |
| your gpsd | the locator from GPS | `station.gpsd` (none) |

On the local network: DHCP (unless static), mDNS (`<hostname>.local`,
UDP 5353), and the unit's own services: the web page and the admin pages
on TCP 8073 (HTTP and HTTPS on the same port, with the unit's own
certificate), HPSDR clients on UDP 1024 (`hpsdr.enabled`, on), SSH on
TCP 22 only with a key in `web888.txt`.

Listeners' browsers fetch the page and its data from the unit only.

## 6. When something is wrong

Work down the list:

1. **The receiver page works but something is off:** the admin page's
   **Logs** (web888d, the whole system, the kernel, `web888-setup.log`)
   and **Status**. Download a log to send it with a report.
2. **The page doesn't come up, but the unit is on the network** (the
   router sees it): look at `web888-setup.log` on the card. A mistyped
   network setting is the usual cause.
3. **The system keeps failing to start:** the unit goes back to the
   other system slot by itself, and with neither working it starts in
   **safe mode**: the admin page (settings, logs, updates) without the
   radio, also at the fixed address `http://169.254.88.8:8073` (a
   computer plugged straight into the unit). See
   [safe-mode.md](safe-mode.md). Putting `safe = yes` in `web888.txt`
   starts it that way on purpose.
4. **Nothing at all on the network, after the boot loader was written:**
   on a computer, rename `boot.bin.old` and `u-boot.img.old` on the
   `WEB888` drive to `boot.bin` and `u-boot.img` (replacing them).
5. **Still nothing:** write the card image again (§1), put back your
   `web888.txt` settings and restore the settings backup (§4).
6. **The stock firmware**, to check the hardware: the original card.

A 3.3 V USB-serial adapter on the board's console UART (115200 8N1)
shows how far a boot gets: `U-Boot SPL`, `U-Boot`, `web888: ADC clock
122.88 MHz`, `web888: slot A`, then the kernel. Send that output with a
report if the unit doesn't start.

## 7. The card

| partition | size | what |
|---|---|---|
| 1, FAT `WEB888` | 64 MB | the boot loader (`boot.bin`, `u-boot.img`), `web888.txt`, `web888-setup.log` |
| 2, slot A | 512 MB | the system (read-only) |
| 3, slot B | 512 MB | the second system, for updates and as a fallback |
| 4, `config` | 32 MB | the admin page's settings, the passwords' hashes, tunnel secrets, SSH host keys |

In normal use the card is written only when a setting is saved, at boot
when `web888.txt` changed, and by an update. A watchdog restarts the
unit if the system hangs.
