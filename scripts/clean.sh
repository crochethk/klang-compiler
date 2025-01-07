#!/usr/bin/env bash

#
# THIS IS INTENTIONALLY HARDCODED TO PREVENT FATAL DELETIONS CAUSED BY MISCONFIGURATION.
#

rm -rf ./build
if [ "$1" = "all" ]; then
    rm -rf ./src/main/.antlr ./src/main/gen
fi