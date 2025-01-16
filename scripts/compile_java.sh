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

# Create a file with a list of all java files in the specified directories.
# Files that have a more recent class file counterpart in the output directory
# are skipped (i.e. only changed files are collected).
# Parameters:
#   $1 : Full filename, the list will be written to. Will be overwritten, if existing!
#   $2 : Directories as array reference (passed as: "myArr[@]").
#   $3 : Directory where to look for existing class files.
_collect_changed_sources() {
    if [[ $# -ne 3 ]]; then
        echo "'${0}' (${LINENO}): $# args received but expected 3"
        return 1
    fi
    local dirs=("${!2}") # get array

    mkdir -p $(dirname "${1}")
    rm -f "${1}"

    # Write list of all java files to be compiled
    for d in "${dirs[@]}"; do
        local java_files=( $(find "${d}" -type f -name "*.java") )

        # Filter source files w/ up to date .class file
        for java_file in "${java_files[@]}"; do
            # Get corresponding class filepath
            # - replace extension
            local class_file="${java_file%.java}.class"
            # - replace base directory
            class_file="${class_file/#${d}/${3}}"

            # Append java_file if class_file not existent or outdated
            if [[ ! -f "${class_file}" || "${java_file}" -nt "${class_file}" ]]; then
                # echo "MODIFIED: '${java_file}'"
                echo "${java_file}" >> "${1}"
            fi
        done
    done
}

# Parameters:
#   $1 : Target workdir (directory where files for this target will be saved)
#   $2 : Array ref with paths to dependencies
#   $3 : Array ref with source directories
_compile_sources() {
    if [[ $# -ne 3 ]]; then
        echo "'${0}' (${LINENO}): $# args received but expected 3"
        return 1
    fi
    # where to put all .class files
    local work_dir="${1}"
    local classes_out_dir="${work_dir}/classes"
    local sourcesListFile="${work_dir}/sources.txt"

    local source_dirs=("${!3}")
    _collect_changed_sources "${sourcesListFile}" "source_dirs[@]" "${classes_out_dir}"

    # compile source files
    local classpath=$(_join_array "${2}" ":")

    if [[ -s "${sourcesListFile}" ]]; then
        _compile_file_list "${sourcesListFile}" "${classes_out_dir}:${classpath}" "${classes_out_dir}"
    else
        echo "INFO: javac skipped (already up to date)."
    fi
}

_run_antlr_if_necessary() {
    mkdir -p "${ANTLR_OUT_BASE}"

    local all_files=$( find "${ANTLR_OUT_BASE}" -type f )
    if [[ -z "${all_files}" ]]; then
        # no files exist at all
        ./scripts/run_antlr.sh
    else
        # Check for outdated files, compared to grammar file
        local old_files=$(find "${ANTLR_OUT_BASE}" -type f -not -newer "${ANTLR_GRAMMAR_FILE}")
        if [[ -n "${old_files}" ]]; then
            ./scripts/run_antlr.sh
        fi
    fi
}

compile_dev() {
    _run_antlr_if_necessary
    _compile_sources "${DEV_WORK_DIR}" "DEV_DEPENDENCIES[@]" "DEV_SRC_DIRS[@]"
}

compile_release() {
    _run_antlr_if_necessary
    _compile_sources "${RELEASE_WORK_DIR}" "DEPENDENCIES[@]" "RELEASE_SRC_DIRS[@]"
}

# Do not execute if the script is being sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    if [[ "${1}" == "dev" ]]; then
        compile_dev
        exit_code=$?
    elif [[ $# -eq 0 || "${1}" == "release" ]]; then
        compile_release
        exit_code=$?
    else
        echo "Invalid arguments provided"
        exit_code=1
    fi
    if [[ $exit_code -ne 0 ]]; then
        echo -e "\e[31m>>> ERROR while compiling java source files\e[0m\n"
        exit 1
    fi
fi
