# Self-hosted runners (Yocto, Vivado)

One VM, two runner instances sharing one container image:

* `os`: builds the image for pushes to `main` and tags of this repository
  (never for pull requests; see `.github/workflows/build.yml`), label
  `yocto`.
* `hw`: builds the FPGA bitstreams of web888-ng-hw (its
  `.github/workflows/fpga.yml`), label `vivado`. Vivado is installed on
  the VM under `/data/vivado` and mounted read-only into the job: it is
  never part of the image (its licence forbids redistribution).

One container per job, discarded afterwards. Only the Yocto caches in `/srv/yocto` persist
(downloads, sstate, and `repos/`: kas reference clones of the layers),
and nginx serves them read-only as a mirror for local builds.

## VM

| | |
|---|---|
| vCPU | 24 (one socket of a 2 × 12-core host is plenty) |
| RAM | 64 GB (48 GB works) |
| disk | 32 GB system + 500 GB thin SSD data disk at `/srv/yocto` (ext4 or xfs, `relatime` so pruning can use access times) |
| OS | Debian 13 (trixie) |
| network | own VLAN/DMZ; outbound internet only (`nftables.conf`) |

A clean build takes roughly 1.5–2.5 h on 24 Haswell threads (estimate);
incremental builds from the shared state take minutes.

## Setup

As root on the VM:

```
apt install podman uidmap slirp4netns nginx nftables
adduser --system --group --home /var/lib/runner --shell /bin/sh runner
usermod --add-subuids 200000-265535 --add-subgids 200000-265535 runner
loginctl enable-linger runner

mkdir -p /srv/yocto/downloads /srv/yocto/sstate   # or bind-mount a data disk there
chown -R runner:runner /srv/yocto

install -m 755 web888-runner.sh /usr/local/bin/
install -d -m 750 -g runner /etc/web888-runner
for f in env os.env hw.env; do install -m 640 -g runner /dev/null /etc/web888-runner/$f; done
#   /etc/web888-runner/env (shared):
#     GH_PAT=<fine-grained token>
#     RUNNER_WORK=/data/runner-work   (job workspaces on the data disk; a
#                                      build needs ~60 GB, the system disk won't do)
#   /etc/web888-runner/os.env:
#     RUNNER_REPO=jeskko/web888-ng-os
#     RUNNER_LABELS=yocto
#     RUNNER_PODMAN_ARGS="-v /srv/yocto:/srv/yocto"
#   /etc/web888-runner/hw.env (see "Vivado" below):
#     RUNNER_REPO=jeskko/web888-ng-hw
#     RUNNER_LABELS=vivado
#     RUNNER_PODMAN_ARGS="--network host -v /data/vivado:/data/vivado:ro -v /etc/web888-runner/xilinx.lic:/opt/xilinx.lic:ro -e XILINXD_LICENSE_FILE=/opt/xilinx.lic"
install -m 644 web888-runner@.service /etc/systemd/system/

install -m 644 nginx-mirror.conf /etc/nginx/sites-available/yocto-mirror
ln -sf ../sites-available/yocto-mirror /etc/nginx/sites-enabled/default

#   set GATEWAY, DNS (and LAN ranges) in nftables.conf, check with
#   `nft -c -f nftables.conf`, then apply with a rollback timer in case
#   the rules lock you out (an ssh session opened before the first
#   firewall may hang; open a new one to test):
systemd-run --on-active=180 --unit=nft-rollback nft flush ruleset
install -m 755 nftables.conf /etc/nftables.conf && nft -f /etc/nftables.conf
#   new ssh login works? then:
systemctl stop nft-rollback.timer
systemctl enable --now nftables nginx

# build the runner image as the runner user (rootless podman)
cp Containerfile entrypoint.sh /var/lib/runner/
runuser -u runner -- env XDG_RUNTIME_DIR=/run/user/$(id -u runner) \
    sh -c 'cd ~ && podman build -t localhost/web888-runner:latest .'
systemctl enable --now web888-runner@os web888-runner@hw
```

The **token**: one fine-grained personal access token limited to the
repositories `jeskko/web888-ng-os` and `jeskko/web888-ng-hw` with the
permission
**Administration: read and write** (needed to create runner registration
tokens), and nothing else. It never enters the job container; each job
gets a one-hour registration token instead.

Finally set the repository variable `SELF_HOSTED_RUNNER` to `true` in
both repositories (Settings → Secrets and variables → Actions →
Variables), which turns on the jobs that need these runners.

## Vivado (hw instance)

* Install Vivado (same version as `fpga/` is built with, 2026.1; Zynq-7000
  devices are enough) under `/data/vivado`, readable by `runner`. The
  workflow calls `/data/vivado/2026.1/Vivado/bin/vivado`.
* Vivado 2026.1 needs a licence even for the free devices: a node-locked
  Basic licence for the MAC of the VM's network interface, installed as
  `/etc/web888-runner/xilinx.lic` (root:runner, 0640). The hw container
  runs with `--network host` so that Vivado sees that MAC (a rootless
  container otherwise gets a random one). The host firewall still applies.
* The job sets `LD_LIBRARY_PATH=/opt/vivado-compat` (ncurses5/tinfo5
  names) and `LD_PRELOAD=/lib/x86_64-linux-gnu/libudev.so.1` (Vivado's
  device scan crashes in libudev otherwise).
* The two instances may run at the same time (a Vivado run needs a few
  GB of RAM and 8 threads).

## Migrating from the single-instance setup

```
systemctl disable --now web888-runner
rm /etc/systemd/system/web888-runner.service
cd /etc/web888-runner && grep -v '^RUNNER_REPO=' env > env.new   # keep GH_PAT, RUNNER_WORK
printf 'RUNNER_REPO=jeskko/web888-ng-os\nRUNNER_LABELS=yocto\nRUNNER_PODMAN_ARGS="-v /srv/yocto:/srv/yocto"\n' > os.env
#   write hw.env and xilinx.lic as above; then fix modes:
mv env.new env && chown root:runner env os.env hw.env xilinx.lic && chmod 640 env os.env hw.env xilinx.lic
#   then the updated script, unit and image as in Setup:
install -m 755 web888-runner.sh /usr/local/bin/
install -m 644 web888-runner@.service /etc/systemd/system/
cp Containerfile entrypoint.sh /var/lib/runner/
runuser -u runner -- env XDG_RUNTIME_DIR=/run/user/$(id -u runner) \
    sh -c 'cd ~ && podman build -t localhost/web888-runner:latest .'
systemctl daemon-reload && systemctl enable --now web888-runner@os web888-runner@hw
```

Old job workspaces move from `$RUNNER_WORK` to `$RUNNER_WORK/<instance>`;
the old contents can be deleted.

## Maintenance

* Prune the shared state monthly, e.g. remove entries not accessed
  for 60 days: `find /srv/yocto/sstate -type f -atime +60 -delete`, or use
  `sstate-cache-management.py` from oe-core.
* New runner release: bump `RUNNER_VERSION` in `Containerfile`, rebuild
  the image. The runner also self-updates within a job when needed.

## Local builds against the mirror

```
WEB888_MIRROR=http://<runner-vm> kas-container build kas/web888.yml:kas/mirror.yml
```
