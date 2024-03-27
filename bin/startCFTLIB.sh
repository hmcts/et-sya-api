#!/usr/bin/env sh

set -eu

./bin/kill-residual-processes.sh

./gradlew bootRun --args='--spring.profiles.active=cftlib'
