#!/usr/bin/env sh
# Prevent multiple sourcing
if [ -z "${CONFIG_SH_SOURCED}" ]; then
    CONFIG_SH_SOURCED=1
    # --------------------------------------------------------------------------

    # ---- Antlr config
    ANTLR_GRAMMAR_FILE="src/main/Klang.g4"
    ANTLR_JAR=lib/antlr4-4.13.2-complete.jar
    ANTLR_GEN_FILES_PACKAGE=cc.crochethk.klang.antlr
    ANTLR_OUT_BASE="src/main/gen/"
    # ----

    DEPENDENCIES=(                       \
        'lib/antlr4-4.13.2-complete.jar' \
        'lib/lombok/lombok-1.18.36.jar'  \
    )

    DEV_DEPENDENCIES=(          \
        "${DEPENDENCIES[@]}"    \
        'lib/junit5/junit-platform-console-standalone-1.11.4.jar' \
    )

    BUILD_ARTIFACTS_BASE_DIR="./build"

    RELEASE_WORK_DIR="${BUILD_ARTIFACTS_BASE_DIR}/release"
    RELEASE_SRC_DIRS=(  \
        'src/main/java' \
        'src/main/gen'  \
    )

    DEV_WORK_DIR="${BUILD_ARTIFACTS_BASE_DIR}/dev"
    DEV_SRC_DIRS=(      \
        'src/main/java' \
        'src/main/gen'  \
        'src/test/java' \
    )

    TEST_WORK_DIR="${BUILD_ARTIFACTS_BASE_DIR}/test"

    TEST_ASM_TESTS_DIR=src/test/c/gen_asm
    TEST_KLANG_FILES_DIR=tests

    # JAR build config
    JAR_WORK_DIR="${BUILD_ARTIFACTS_BASE_DIR}/jar"

    # --------------------------------------------------------------------------
    # Helper functions
    # --------------------

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
fi
