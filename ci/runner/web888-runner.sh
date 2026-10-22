#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Runs on the runner VM as the unprivileged user `runner`, one process per
# runner instance (systemd unit web888-runner@<instance>.service).
# Forever: fetch a registration token, start one ephemeral runner
# container, wait for it to finish its job.
#
#   web888-runner.sh <instance>        e.g. os, hw
#
# /etc/web888-runner/env (root:runner, 0640), shared:
#   GH_PAT=<fine-grained PAT: the runner repos only, "Administration: read
#           and write">
#   RUNNER_WORK=/data/runner-work   (optional; job workspaces, on the big
#                                    disk: a Yocto build needs ~60 GB)
# /etc/web888-runner/<instance>.env (root:runner, 0640):
#   RUNNER_REPO=jeskko/web888-ng-os
#   RUNNER_LABELS=yocto              (job routing: `runs-on` labels)
#   RUNNER_PODMAN_ARGS="-v /srv/yocto:/srv/yocto"   (optional, extra
#                                    podman run arguments: mounts, -e NAME=value
#                                    for the jobs, --network host, ...)
set -eu
inst=${1:?usage: web888-runner.sh <instance>}
. /etc/web888-runner/env
. "/etc/web888-runner/$inst.env"
# rootless podman from a system service: use the lingering user manager
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-/run/user/$(id -u)}"
export DBUS_SESSION_BUS_ADDRESS="${DBUS_SESSION_BUS_ADDRESS:-unix:path=$XDG_RUNTIME_DIR/bus}"
IMAGE=localhost/web888-runner:latest
WORK=${RUNNER_WORK:-/var/lib/runner/work}/$inst
while :; do
    tok=$(curl -fsS -X POST \
        -H "Authorization: Bearer $GH_PAT" \
        -H "Accept: application/vnd.github+json" \
        "https://api.github.com/repos/$RUNNER_REPO/actions/runners/registration-token" |
        sed -n 's/.*"token": *"\([^"]*\)".*/\1/p')
    if [ -z "$tok" ]; then
        echo "no registration token, retrying in 60 s" >&2
        sleep 60
        continue
    fi
    # fresh container and empty workspace per job; only mounted caches and
    # tools persist
    mkdir -p "$WORK" && find "$WORK" -mindepth 1 -delete
    # fixed name + --replace: a service restart leaves no second runner
    # behind (rootless containers outlive the service's processes)
    # shellcheck disable=SC2086
    podman run --rm --name "web888-runner-$inst" --replace \
        --userns=keep-id:uid=30000,gid=30000 \
        -e RUNNER_REPO="$RUNNER_REPO" -e RUNNER_TOKEN="$tok" \
        -e RUNNER_LABELS="${RUNNER_LABELS:-}" \
        -v "$WORK":/builder/work ${RUNNER_PODMAN_ARGS:-} \
        "$IMAGE" || sleep 10
done
