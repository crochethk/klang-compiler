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

source ./scripts/compile_java.sh

work_dir="${BUILD_ARTIFACTS_BASE_DIR}/jar"
classes_out_dir="${work_dir}/classes/"

# Make sure lexer and parser were generated
./scripts/run_antlr.sh

# Compile compiler and copy to workdir
compile_release
if [ $? -ne 0 ]; then
    echo -e ">>> ERROR while compiling java source files\n"
    exit 1
fi
cp -r "${BUILD_ARTIFACTS_BASE_DIR}/release" "${work_dir}"

# Extract libraries to be included
cwd_bak=$(pwd)
cd "${classes_out_dir}"
jar xf "${cwd_bak}/lib/antlr4-4.13.2-complete.jar"
cd "${cwd_bak}"

# Create jar containing all class files, using the specified main class
# jar path and filename
jar_path="${BUILD_ARTIFACTS_BASE_DIR}/klangc.jar"
jar --create --file "${jar_path}" --main-class cc.crochethk.compilerbau.praktikum.KlangCompiler -C "${classes_out_dir}" .
echo "Jar created: '${jar_path}'"
