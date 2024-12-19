#!/bin/sh

# ==============================================================================
# This script builds a self-contained JAR of the klang compiler.
#
# To run it using configuration from a ".env" or with the defaults type:
#       java --enable-preview -jar klangc.jar
# ==============================================================================

JAR_PATH=./klangc.jar

BUILD_ARTIFACTS_DIR=./build
JAVA_SOURCES_ROOT=./src/main

javacOutDir=${BUILD_ARTIFACTS_DIR}/javac/
sourcesListFile=${BUILD_ARTIFACTS_DIR}/sources.txt

mkdir -p "${BUILD_ARTIFACTS_DIR}" "${javacOutDir}"

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
