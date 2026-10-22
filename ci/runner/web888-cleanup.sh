#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Weekly build host cleanup (web888-cleanup.timer), as the user `runner`:
#  * Yocto shared state not used for SSTATE_DAYS days (default 60; the data
#    disk is mounted relatime, so access times show use). Downloads stay:
#    they are the source mirror, and BitBake does not read (touch) them
#    once fetched, so their access times say nothing about use.
#  * podman: dangling images (left by runner image rebuilds) and stopped
#    containers.
# Logs what it did and the disk usage to the journal.
set -eu
SSTATE=${SSTATE:-/srv/yocto/sstate}
SSTATE_DAYS=${SSTATE_DAYS:-60}
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-/run/user/$(id -u)}"
cd /

if [ -d "$SSTATE" ]; then
    n=$(find "$SSTATE" -type f -atime +"$SSTATE_DAYS" -print -delete | wc -l)
    find "$SSTATE" -mindepth 1 -type d -empty -delete
    echo "sstate: removed $n files unused for $SSTATE_DAYS days"
fi
podman container prune -f >/dev/null
podman image prune -f | sed 's/^/podman: removed image /'
du -sh /srv/yocto/* 2>/dev/null | sed 's/^/usage: /'
df -h /srv/yocto | tail -n 1 | sed 's/^/disk: /'
