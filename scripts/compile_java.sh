#!/usr/bin/env bash

BUILD_ARTIFACTS_BASE_DIR="./build"

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

# Compile a list of java files.
# Parameters:
#   $1 : sources.txt filepath
#   $2 : classpath(s) according to javac "-cp"-format (e.g. "bin/:lib/*.jar:")
#   $3 : output directory
compile_file_list() {
    if [[ $# -ne 3 ]]; then
        echo "$# args received but expected 3"
        return 1
    fi
    cmd="javac --enable-preview --source 23 --target 23 -cp \"${2}\" -d \"${3}\" @\"${1}\""
    echo "${cmd}"
    eval ${cmd}
}
