#!/usr/bin/env bash

# ==============================================================================
# This script, when directly executed, will bulk compile all klang files within
# the given directory and its subdirectories.
# It's assumed that the project was already compiled using
# "scripts/compile_java -> compile_release".
#
# Parameters:
#   $1 : Output directory for generated files.
#   $2 : Directory where to look for klang files.
#
# Example:
#   scripts/compile_klang.sh ./build/out ./tests
# ==============================================================================

source ./scripts/config.sh

# Compile a list of klang files.
# Parameters:
#   $1    : output directory for generated files.
#   $2... : One or more source file paths to compile.
#           Note that __RELATIVE PATHS__ are expected here.
compile_klang_files() {
    local outdir="${1}"
    shift
    local files=${@}

    local classpath=$(_join_array "DEPENDENCIES[@]" ":")
    # Add compiler classes to cp
    classpath="${RELEASE_WORK_DIR}/classes:${classpath}"
 
    java --enable-preview -cp "${classpath}" \
        cc.crochethk.compilerbau.praktikum.KlangCompiler --output "${outdir}" -- $files
}

# Do not execute if the script is being sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    compile_klang_files "${1}" "$(find "${2}" -regex ".+\.[kK]$")"
fi
