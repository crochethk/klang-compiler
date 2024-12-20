#!/bin/sh

rm -rf ./build ./code_gen
if [ "$1" = "all" ]; then
    rm -rf ./src/main/.antlr ./src/main/gen
fi