#!/usr/bin/env sh
# Prevent multiple sourcing
if [ -z "${CONFIG_SH_SOURCED}" ]; then
    CONFIG_SH_SOURCED=1
    # --------------------------------------------------------------------------

    DEPENDENCIES=(                       \
        'lib/antlr4-4.13.2-complete.jar' \
        'lib/lombok/lombok-1.18.36.jar'  \
    )

    DEV_DEPENDENCIES=(          \
        "${DEPENDENCIES[@]}"    \
        'lib/junit5/junit-platform-console-standalone-1.11.4.jar' \
    )

    BUILD_ARTIFACTS_BASE_DIR="./build"

    # --------------------------------------------------------------------------
fi
