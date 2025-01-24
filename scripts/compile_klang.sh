#!/usr/bin/env bash

# ==============================================================================
# This script, when directly executed, will if given...
#   - ...a FILE_PATTERN as INPUT:   Compile all files matching the given pattern
#   - ...a DIRECTORY as INPUT:      Compile all klang files within the given
#           directory and its subdirectories
#
# Parameters:
#   $1 : Output format ("jbc"|"asm"|"all")
#   $2 : Output directory for generated files.
#   $3 : Input klang file(pattern) or directory where to look for klang files.
#
# Example:
#   # Compile all ".k" files in "./tests/**"
#   scripts/compile_klang.sh all ./build/out ./tests
#
#   # Compile all ".k" files in "./tests/" matching pattern "test_foo_*"
#   scripts/compile_klang.sh all ./build/out "./tests/test_foo*"
# ==============================================================================

source ./scripts/config.sh
source ./scripts/compile_java.sh

# Compile a list of klang files.
# Parameters:
#   $1    : Which code to generate ("jbc"|"asm"|"all").
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
    compile_release

    local outdir="${2}"
    shift 2
    local files=${@}

    local classpath=$(_join_array "DEPENDENCIES[@]" ":")
    # Add compiler classes to cp
    classpath="${RELEASE_WORK_DIR}/classes:${classpath}"
 
    java --enable-preview -cp "${classpath}" \
        cc.crochethk.klang.KlangCompiler ${output_format_flags[@]} --output "${outdir}" -- $files
}

# Do not execute if the script is being sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    if [[ $# -lt 3 ]]; then
        echo "Provide at least 3 arguments"
        exit 1
    fi

    outformat="$1"
    outdir="$2"

    if [[ -d "$3" ]]; then
        # $3 is a directory
        compile_klang_files "$outformat" "$outdir" "$(find "$3" -regex ".+\.[kK]$")"
    else
        # $3 is a filepattern or list of files
        shift 2
        compile_klang_files "$outformat" "$outdir" \
            "$(find $@ -maxdepth 1 -wholename "*.k" -o -wholename "*.K" )"
    fi
fi
