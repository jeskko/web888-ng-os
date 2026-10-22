# Self-hosted Yocto runner

Builds the image for pushes to `main` and tags of this repository (never
for pull requests; see `.github/workflows/build.yml`). One container per
job, discarded afterwards. Only the Yocto caches in `/srv/yocto` persist,
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
install -m 640 -g runner /dev/null /etc/web888-runner/env
#   edit /etc/web888-runner/env:
#     RUNNER_REPO=jeskko/web888-ng-os
#     GH_PAT=<fine-grained token>
#     RUNNER_WORK=/data/runner-work   (job workspace on the data disk; a
#                                      build needs ~60 GB, the system disk won't do)
install -m 644 web888-runner.service /etc/systemd/system/

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
systemctl enable --now web888-runner
```

The **token**: a fine-grained personal access token limited to the
repository `jeskko/web888-ng-os` with the permission
**Administration: read and write** (needed to create runner registration
tokens), and nothing else. It never enters the job container; each job
gets a one-hour registration token instead.

Finally set the repository variable `SELF_HOSTED_RUNNER` to `true`
(Settings → Secrets and variables → Actions → Variables), which turns on
the `image` job.

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
