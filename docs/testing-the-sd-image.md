# Testing the SD card image on a Web-888

This is for a Web-888 owner who has the unit at hand and is willing to try
web888-ng's SD card image. It replaces the whole firmware: our own boot
loader, kernel, FPGA bitstream and the `web888d` server, all from the card.
Nothing on the unit itself is written, so putting your original card back
brings the stock firmware back exactly as it was.

**Status: first hardware test.** The image has only run in the emulator so
far. The parts the emulator can't check are listed in §4; that is what
this test is for.

## 1. What you need

* The Web-888 with power, Ethernet and an HF antenna.
* A **spare** microSD card of 2 GB or more. Keep your original card as it
  is: it is the way back.
* A computer that can write SD cards (Raspberry Pi Imager, balenaEtcher,
  `bmaptool` or `dd`).
* Optional, but very useful if it doesn't boot: a 3.3 V USB-serial
  adapter on the board's console UART (115200 8N1).

## 2. Write the card

1. Download `web888-ng-sd-<version>.img.xz` and `SHA256SUMS` from the
   release, and check the file: `sha256sum -c SHA256SUMS --ignore-missing`.
2. Write the image to the spare card. The imaging tools take the `.xz` as
   it is. With `dd`:
   `xz -dc web888-ng-sd-<version>.img.xz | sudo dd of=/dev/sdX bs=4M conv=fsync`
   (`/dev/sdX` is the card; check with `lsblk` first).
3. Take the card out and put it back in. A drive called `WEB888` appears.
   Open `web888.txt` in a text editor (Notepad is fine). Settings you may
   want:
   * `admin_password = ...`: the password for the admin pages (8
     characters or more). Without one, the unit makes one and writes it to
     `web888-password.txt` on the card.
   * `network = static`, with `address`, `gateway` and `dns`, if your
     network has no DHCP server. The default is DHCP.
   * `ssh_key = ssh-ed25519 AAAA...`: your SSH public key, if you want a
     shell on the unit (`ssh root@<unit>`). Without a key, SSH is off.
   Remove the `#` in front of a setting to use it. Save and eject the card.

## 3. Boot it

1. Power the Web-888 off. Swap in the new card. Connect Ethernet and the
   antenna, then power on.
2. After about half a minute, open `http://web888.local:8073` (or the
   address your router gave the unit; its name is `web888`).
3. Press "Start audio" and tune around: the waterfall should show the
   band and the audio should sound as it does with the stock firmware.
   Over `http://` the status bar notes the extra audio delay and links to
   the same page over `https://`, which plays with less (the browser
   warns once about the unit's own certificate).
4. The admin pages are under `#/admin` (the gear icon). Log in with the
   password from step 2.3, or the one in `web888-password.txt`.

Each boot writes `web888-setup.log` on the card. It shows what was taken
from `web888.txt` and any problem with a setting (say, a mistyped address).
Look at it whenever something isn't as you set it.

To go back to the stock firmware: power off and put your original card
back in.

## 4. What to check and report

The emulator can't check these. Each one matters:

| | check | how |
|---|---|---|
| 1 | It boots and the web page comes up | §3 |
| 2 | **Signals at the right frequency**: a broadcast station, a time signal (e.g. 10 MHz) or an FT8 band where you expect it | the waterfall; FT8 spots in the UI |
| 3 | Signal levels as with the stock firmware on the same antenna | compare a known station |
| 4 | The network: DHCP, or your static address; the same address after a power cycle | your router / `web888-setup.log` |
| 5 | The MAC address is the one the stock firmware used | your router's client list, or `web888-setup.log` |
| 6 | It keeps running: leave it on for a day | the web page still works |
| 7 | Power cuts: pull the power while it runs, and while changing the password (power on, pull it within a minute); it must boot again | §3 |
| 8 | GPS, if you have the GPS option: time and position in the admin page | admin page |

How it can fail, and what each case suggests:

* **No web page and no address on the network.** The boot loader or the
  kernel didn't come up. A serial console shows how far it got: the
  first lines are `U-Boot SPL`, then `U-Boot`, then `web888: ADC clock
  122.88 MHz`, `web888: slot A`, and the kernel's log. Please send that
  output.
* **The page works but the waterfall is empty or noise only.** Most likely
  the ADC clock. Send `web888-setup.log` and, with SSH, the output of
  `journalctl -b -u web888d`.
* **Signals are off frequency by a constant factor.** The clock's
  reference differs from 24.576 MHz on your board. Send your unit's
  board data (`fw_printenv` on the stock firmware shows `refclock`).

When reporting, please include: the image version, your board's `hw_rev`
and serial (the admin page's System card, or `fw_printenv` on the stock
firmware), `web888-setup.log`, and what you tried.

## 5. How the card is laid out

| partition | size | what |
|---|---|---|
| 1, FAT `WEB888` | 64 MB | the boot loader (`BOOT.BIN`, `u-boot.img`), `web888.txt`, the setup log |
| 2, slot A | 512 MB | the system (read-only) |
| 3, slot B | 512 MB | a second copy of the system, for updates and as a fallback |
| 4, `config` | 32 MB | settings made in the admin pages, the admin password's hash |

The system partitions are never written in normal use. If slot A fails to
boot three times in a row, the boot loader starts slot B. The unit's
watchdog resets it if the system hangs.
