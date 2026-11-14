# Safe mode

A unit whose system keeps failing to start would otherwise try again
forever, out of reach. In safe mode it starts **everything except the
radio**, so that it can be reached and repaired:

* the network (as `web888.txt` sets it up), SSH and the tunnels;
* **one more address that is always the same: `169.254.88.8`**, whatever
  the network settings or the DHCP server do. A computer on the same
  network segment reaches it with an address in 169.254.x.x of its own
  (most systems take one by themselves on a cable with no DHCP server;
  otherwise add one by hand, for example 169.254.88.1/16);
* web888d's pages: the admin page with the settings, the logs and
  **Update**, with a notice that says why the unit is in safe mode;
* the watchdog and the update system (RAUC).

Not started: the SDR driver's use, the bitstream, the receivers, the
skimmers and their uploads, HPSDR. web888d runs with `--safe`.

## When

* **By itself**, when neither root slot has tries left: each slot gets
  three boots to bring web888d up (`docs/updates.md`), and after the last
  of them the unit is in safe mode. From the boot after that, the boot
  loader also keeps the SDR driver from loading (`web888.safe=1` on the
  kernel command line) and takes the two slots in turn, in case one of
  them doesn't boot at all.
* **Asked for**: `safe = yes` in `web888.txt` on the card.

`web888-setup.log` on the card and the admin page say which.

## Leaving it

* The admin page's **Leave safe mode** (the unit reboots), or over SSH
  `web888-safe leave`: both slots get their tries back and the normal
  system starts. If it fails again, safe mode comes back after the same
  number of boots.
* Installing an update (the **Update** tab or `rauc install`): the new
  slot has its tries, so the reboot starts it.
* `safe = yes`: remove the line from `web888.txt`.

Until then the unit stays in safe mode over reboots and power cuts.

## Limits

* Safe mode needs a kernel that boots and a root file system that
  mounts in at least one slot. It is for faults in what starts later:
  the driver, the bitstream, web888d.
* The boot loader's part (no driver, the slots in turn) needs the boot
  loader of v0.9.3 or later. Updates don't replace the boot loader, so a
  card first written with an earlier release has the rest: safe mode on
  the last try of the last slot, for that boot; a reboot there starts
  the count again.
* Two units in safe mode on one network segment would both have
  169.254.88.8.

## Tested

In the emulator (web888-ng-hw `emu/tests/sd-image.sh`, step 4): safe
mode from `web888.txt` and from the boot loader with no tries left, the
address, web888d without the radio, the driver not loaded, and back to
the normal system with `web888-safe leave`.
