#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

source ./scripts/config.sh

# figure out package-adjusted path for generated files
antlrFilesPackageAsDir=$(echo "${ANTLR_GEN_FILES_PACKAGE}" | tr '.' '/')
antlrOutputDir="${ANTLR_OUT_BASE}/${antlrFilesPackageAsDir}"

# generate antlr lexer and parser
java -jar "${ANTLR_JAR}" -message-format antlr -Xexact-output-dir -o "${antlrOutputDir}" \
    -package "${ANTLR_GEN_FILES_PACKAGE}" -listener -no-visitor "${ANTLR_GRAMMAR_FILE}"
