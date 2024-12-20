#!/usr/bin/env bash

BUILD_ARTIFACTS_DIR="./build"

# Compile a list of all java files in the specified directories.
# Parameters:
#   $1 : Directories as array reference (passed as: "myArr[@]")
#   $2 : Full filename, the list will be written to. Will be overwritten, if existing!
collect_sources() {
    dirs=("${!1}") # get array

    mkdir -p $(dirname "${2}")
    rm -f "${2}"

    # write list of all java files to be compiled
    for d in "${dirs[@]}"; do
        find "${d}" -type f -name "*.java" >> "${2}"
    done
}
