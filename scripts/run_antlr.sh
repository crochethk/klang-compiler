#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# antlr config
ANTLR_JAR=lib/antlr4-4.13.2-complete.jar
ANTLR_FILES_PACKAGE=cc.crochethk.compilerbau.praktikum.antlr
GRAMMAR_FILE=src/main/Klang.g4

# figure out directory for generated files
antlrFilesPackageAsDir=$(echo "${ANTLR_FILES_PACKAGE}" | tr '.' '/')
antlrOutputDir=src/main/gen/${antlrFilesPackageAsDir}

# generate antlr lexer and parser
java -jar "${ANTLR_JAR}" -message-format antlr -Xexact-output-dir -o "${antlrOutputDir}" \
    -package "${ANTLR_FILES_PACKAGE}" -listener -no-visitor "${GRAMMAR_FILE}"
