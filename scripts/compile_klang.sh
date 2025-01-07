#!/usr/bin/env bash

# ==============================================================================
# This script, when directly executed, will bulk compile all klang files within
# the given directory and its subdirectories.
# Parameters:
#   $1 : Output directory for generated files.
#   $2 : Directory where to look for klang files.
# Example:
#   scripts/compile_klang.sh ./build/out ./tests
# ==============================================================================

source ./scripts/config.sh
source ./scripts/compile_java.sh

# Compile a list of klang files.
# Parameters:
#   $1    : output directory for generated files.
#   $2... : One or more source file paths to compile.
#           Note that __RELATIVE PATHS__ are expected here.
compile_klang_files() {
    local outdir="${1}"
    shift
    local files=${@}

    compile_release
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR while compiling java source files\n"
        exit 1
    fi

    local classpath=$(_join_array "DEPENDENCIES[@]" ":")
    # Add compiler classes to cp
    classpath="${BUILD_ARTIFACTS_BASE_DIR}/release/classes:${classpath}"
 
    java --enable-preview -cp "${classpath}" \
        cc.crochethk.compilerbau.praktikum.KlangCompiler --output "${outdir}" -- $files
}

# Do not execute if the script is being sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    compile_klang_files "${1}" "$(find "${2}" -regex ".+\.[kK]$")"
fi
