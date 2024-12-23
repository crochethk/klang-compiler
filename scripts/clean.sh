#!/usr/bin/env bash

rm -rf ./build
if [ "$1" = "all" ]; then
    rm -rf ./src/main/.antlr ./src/main/gen
fi