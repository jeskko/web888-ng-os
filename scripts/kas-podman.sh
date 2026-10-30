#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
#
# Run kas in its container with rootless podman, as the calling user.
# The container's entrypoint (user switching) doesn't work under rootless
# podman's ID mapping, so it is bypassed.
#
#   KAS_WORK=/data/yocto/web888 scripts/kas-podman.sh build kas/web888.yml:kas/dev.yml
#
# KAS_WORK: build directory (layers, build/, downloads; ~60 GB).
# WEB888_MIRROR: passed on, for kas/mirror.yml.
# WEB888_RAUC_KEY: the bundle's signing key (kas/bundle.yml), mounted
# read-only and passed on as its path in the container.
# The repo is mounted read-only at /repo; build from a plain clone, not a
# git submodule checkout (kas needs a real .git directory), and don't edit
# it while a build runs (BitBake re-parses changed files mid-build).
set -eu
: "${KAS_WORK:?set KAS_WORK to the build directory}"
REPO=$(cd "$(dirname "$0")/.." && pwd)
mkdir -p "$KAS_WORK/.home"
args=
for a in "$@"; do
    # config paths (also colon-separated overlays) live under /repo
    a=$(printf '%s' "$a" | sed 's#\(^\|:\)kas/#\1/repo/kas/#g')
    args="$args $a"
done
# shellcheck disable=SC2086
exec podman run --rm --userns=keep-id --user "$(id -u):$(id -g)" \
    --entrypoint kas \
    -e HOME=/work/.home -e KAS_WORK_DIR=/work -e LANG=C.UTF-8 \
    ${WEB888_MIRROR:+-e WEB888_MIRROR="$WEB888_MIRROR"} \
    ${WEB888_RAUC_KEY:+-v "$WEB888_RAUC_KEY":/keys/rauc.key.pem:ro -e WEB888_RAUC_KEY=/keys/rauc.key.pem} \
    -v "$REPO":/repo:ro -v "$KAS_WORK":/work \
    ghcr.io/siemens/kas/kas:5.5 $args
