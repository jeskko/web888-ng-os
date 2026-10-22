#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Runs on the runner VM as the unprivileged user `runner` (systemd unit
# web888-runner.service). Forever: fetch a registration token, start one
# ephemeral runner container, wait for it to finish its job.
#
# /etc/web888-runner/env (root:runner, 0640):
#   RUNNER_REPO=jeskko/web888-ng-os
#   GH_PAT=<fine-grained PAT: this repo only, "Administration: read and write">
set -eu
. /etc/web888-runner/env
IMAGE=localhost/web888-runner:latest
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
    # fresh container per job; only the Yocto caches persist
    podman run --rm --userns=keep-id:uid=30000,gid=30000 \
        -e RUNNER_REPO="$RUNNER_REPO" -e RUNNER_TOKEN="$tok" \
        -v /srv/yocto:/srv/yocto \
        "$IMAGE" || sleep 10
done
