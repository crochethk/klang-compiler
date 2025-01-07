#!/usr/bin/env bash

source ./scripts/config.sh
source ./scripts/compile_klang.sh

# Compile and execute a ".k" source file.
# Parameters:
#   $1 : Path of the source file to execute. Path may be absolute or relative.
#           In either case, as usual, the directory structure relative to 
#           current directory is interpreted as package (see klangc documentation).
# Example:
#   run_klang_file ./tests/someSubPackage/t6.k
run_klang_file() {
    local outdir="${BUILD_ARTIFACTS_BASE_DIR}/code_gen"

    # Get relative filepath
    local rel_filepath="$(realpath --relative-to="$(pwd)" "${1}")"

    compile_klang_files "${outdir}" "${rel_filepath}"

    local file_dirname="$(dirname ${rel_filepath})"

    # Replace file separators by "."
    local package="$(echo "${file_dirname}" | sed 's/[\\/]/./g')"

    if [ "${package}" == "." ]; then
        package=""
    else
        package="${package}."
    fi

    # Filename without extension (case-insensitive)
    local className="$(basename "${rel_filepath}" | sed 's/\.[kK]$//')"
    local fullClassName="${package}${className}"

    # Run generated JBC
    java -cp "${outdir}" ${fullClassName}
}

# Do not execute if the script is being sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_klang_file "${1}"
fi
