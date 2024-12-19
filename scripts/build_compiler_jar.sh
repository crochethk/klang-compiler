#!/bin/sh
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Builds a self-contained JAR of the klang compiler.
#
# Jar Usage: java --enable-preview -jar klangc.jar [OUTDIR SOURCEFILE [FILES...]]
# - [OUTDIR SOURCEFILE [FILES...]]
#       Semi-Optional outputpath and one or more input source file(s).
#       If not specified a "L1Compiler.env" file must define OUTDIR and SOURCEFILE.
# ==============================================================================

JAR_PATH=./klangc.jar           # The output JAR path and filename
BUILD_ARTIFACTS_DIR=./build
JAVA_SOURCES_ROOT=./src/main    # Parent folder of all java files that will be passed to javac

javacOutDir=${BUILD_ARTIFACTS_DIR}/javac/
sourcesListFile=${BUILD_ARTIFACTS_DIR}/sources.txt
mkdir -p "${BUILD_ARTIFACTS_DIR}" "${javacOutDir}"

# make sure lexer/parser were generated
$(dirname $0)/run_antlr.sh

# create list of all java files to be compiled
find -depth -wholename "${JAVA_SOURCES_ROOT}/*.java" > ${sourcesListFile}

# compile all files specified in "${sourcesListFile}"
javac --enable-preview --source 23 --target 23 -cp "lib/antlr4-4.13.2-complete.jar:" -d "${javacOutDir}" @"${sourcesListFile}"

# extract libraries to be included
cwd_bak=$(pwd)
cd "${javacOutDir}"
jar xf "${cwd_bak}/lib/antlr4-4.13.2-complete.jar"
cd "${cwd_bak}"

# create jar containing all class files, using the specified main class
jar --create --file "${JAR_PATH}" --main-class cc.crochethk.compilerbau.praktikum.L1Compiler -C "${javacOutDir}" .
