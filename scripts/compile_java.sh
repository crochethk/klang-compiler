#!/usr/bin/env bash

source ./scripts/config.sh

# Compile a list of java files.
# Parameters:
#   $1 : sources.txt filepath
#   $2 : classpath(s) according to javac "-cp"-format (e.g. "bin/:lib/*.jar:")
#   $3 : output directory
_compile_file_list() {
    if [[ $# -ne 3 ]]; then
        echo "'${0}' (${LINENO}): $# args received but expected 3"
        return 1
    fi

    # "-proc:full" -> required for lombok (since JDK >22)
    cmd="javac --enable-preview -proc:full --source 23 --target 23 -cp \"${2}\" -d \"${3}\" @\"${1}\""
    echo "${cmd}"
    eval ${cmd}
}

# Compile a list of all java files in the specified directories.
# Parameters:
#   $1 : Full filename, the list will be written to. Will be overwritten, if existing!
#   $2 : Directories as array reference (passed as: "myArr[@]")
_collect_sources() {
    if [[ $# -ne 2 ]]; then
        echo "'${0}' (${LINENO}): $# args received but expected 2"
        return 1
    fi
    local dirs=("${!2}") # get array

    mkdir -p $(dirname "${1}")
    rm -f "${1}"

    # write list of all java files to be compiled
    for d in "${dirs[@]}"; do
        find "${d}" -type f -name "*.java" >> "${1}"
    done
}

#   $1 : Aarray reference whose elements to join
#   $2 : Symbol to use as separator
_join_array() {
    if [[ $# -ne 2 ]]; then
        echo "'${0}' (${LINENO}): $# args received but expected 2"
        return 1
    fi
    local arr=("${!1}")
    old_IFS="$IFS"
    IFS="${2}"
    # Join arr using IFS
    result=$(echo "${arr[*]}")
    # Restore the original value of IFS
    IFS="$old_IFS"
    echo "${result}"
}

# Parameters:
#   $1 : Targetname (basically subdirectory where files will be saved)
#   $2 : Array ref with paths to dependencies
#   $3 : Array ref with source directories
_compile_sources() {
    if [[ $# -ne 3 ]]; then
        echo "'${0}' (${LINENO}): $# args received but expected 3"
        return 1
    fi
    # where to put all .class files
    local work_dir="${BUILD_ARTIFACTS_BASE_DIR}/${1}"
    local classes_out_dir="${work_dir}/classes"
    local sourcesListFile="${work_dir}/sources.txt"

    local source_dirs=("${!3}")
    _collect_sources "${sourcesListFile}" "source_dirs[@]"

    # compile source files
    local classpath=$(_join_array "${2}" ":")
    _compile_file_list "${sourcesListFile}" "${classpath}" "${classes_out_dir}"
}

compile_dev() {
    ./scripts/run_antlr.sh
    local targetname="dev"
    local source_dirs=(     \
        'src/main/java'     \
        'src/main/gen'      \
        'src/test/java'     \
    )
    _compile_sources "${targetname}" "DEV_DEPENDENCIES[@]" "source_dirs[@]"
}

compile_release() {
    ./scripts/run_antlr.sh
    local targetname="release"
    local source_dirs=(     \
        'src/main/java'     \
        'src/main/gen'      \
    )
    _compile_sources "${targetname}" "DEPENDENCIES[@]" "source_dirs[@]"
}
