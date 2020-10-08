#!/usr/bin/env bash
set -eu

if [[ "${CI_TAG:-}" =~ promote-.* ]]; then
    echo "Skipping build as it is a prod promotion"
    exit 0
fi

function log {
   echo "$(date +"%T") - BUILD INFO - $*"
}

export PROJECT_NAME=akvo-lumen

if [ -z "$CI_COMMIT" ]; then
    export CI_COMMIT=local
fi

log Building app
./gradlew build
docker build -t eu.gcr.io/${PROJECT_NAME}/akvo-exact:${CI_COMMIT} -t akvo-exact:prod --rm=false -f Dockerfile .

log Done