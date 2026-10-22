#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
# Register for exactly one job, run it, exit. RUNNER_TOKEN is a short-lived
# registration token (not the PAT), created by web888-runner.sh per job.
set -eu
./config.sh --unattended --ephemeral --replace \
    --url "https://github.com/$RUNNER_REPO" --token "$RUNNER_TOKEN" \
    --name "$(hostname)-$(date +%s)" --labels yocto --work /builder/work
unset RUNNER_TOKEN
exec ./run.sh
