#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Builds a self-contained JAR of the klang compiler.
#
# Jar Usage: java --enable-preview -jar klangc.jar [OUTDIR SOURCEFILE [FILES...]]
# - [OUTDIR SOURCEFILE [FILES...]]
#       Semi-Optional outputpath and one or more input source file(s).
#       If not specified a "klangc.env" file must define OUTDIR and SOURCEFILE.
# ==============================================================================

BUILD_ARTIFACTS_DIR=./build
JAR_PATH=${BUILD_ARTIFACTS_DIR}/klangc.jar  # The output JAR path and filename
JAVA_SOURCES_ROOT=./src/main                # Parent folder of all java files that will be passed to javac

javacOutDir=${BUILD_ARTIFACTS_DIR}/jar/classes/
sourcesListFile=${BUILD_ARTIFACTS_DIR}/jar/sources.txt
mkdir -p "${BUILD_ARTIFACTS_DIR}" "${javacOutDir}"

# make sure lexer/parser were generated
$(dirname $0)/run_antlr.sh

# create list of all java files to be compiled
find "${JAVA_SOURCES_ROOT}" -type f -name "*.java" > ${sourcesListFile}

# compile all files specified in "${sourcesListFile}"
javac --enable-preview --source 23 --target 23 -cp "lib/antlr4-4.13.2-complete.jar:" -d "${javacOutDir}" @"${sourcesListFile}"

if [ $? -ne 0 ]; then
    echo -e ">>> ERROR while compiling java source files\n"
    exit 1
fi

# extract libraries to be included
cwd_bak=$(pwd)
cd "${javacOutDir}"
jar xf "${cwd_bak}/lib/antlr4-4.13.2-complete.jar"
cd "${cwd_bak}"

# create jar containing all class files, using the specified main class
jar --create --file "${JAR_PATH}" --main-class cc.crochethk.compilerbau.praktikum.KlangCompiler -C "${javacOutDir}" .

echo "Jar created: '${JAR_PATH}'"
