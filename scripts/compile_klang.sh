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
#   $1    : Which code to generate ("all", "jbc", "asm").
#   $2    : Output directory for generated files.
#   $3... : One or more source file paths to compile.
#           Note that __RELATIVE PATHS__ are expected here.
compile_klang_files() {
    local output_format_flags=()
    if [[ "$1" == "jbc" ]]; then
        output_format_flags+="--jbc"
    elif [[ "$1" == "asm" ]]; then
        output_format_flags+="--asm"
    elif [[ "$1" == "all" ]]; then
        output_format_flags=("--jbc" "--asm")
    else
        echo "Unknown output code format option: '${1}'"
        return 1
    fi

    local outdir="${2}"
    shift 2
    local files=${@}

    local classpath=$(_join_array "DEPENDENCIES[@]" ":")
    # Add compiler classes to cp
    classpath="${RELEASE_WORK_DIR}/classes:${classpath}"
 
    java --enable-preview -cp "${classpath}" \
        cc.crochethk.compilerbau.praktikum.KlangCompiler ${output_format_flags[@]} --output "${outdir}" -- $files
}

# Do not execute if the script is being sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    compile_klang_files all "${1}" "$(find "${2}" -regex ".+\.[kK]$")"
fi
