#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Builds a self-contained JAR of the klang compiler.
#
# Jar Usage see: java --enable-preview -jar klangc.jar --help
# ==============================================================================

source ./scripts/config.sh
source ./scripts/compile_java.sh

work_dir="${BUILD_ARTIFACTS_BASE_DIR}/jar"
classes_out_dir="${work_dir}/classes/"

# Compile compiler and copy to workdir
compile_release
if [ $? -ne 0 ]; then
    echo -e ">>> ERROR while compiling java source files\n"
    exit 1
fi
cp -r "${RELEASE_WORK_DIR}" "${work_dir}"

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

# Create helper script to execute jar
jar_runner="${BUILD_ARTIFACTS_BASE_DIR}/klangc.sh"

cat > "${jar_runner}" <<- EOM
#!/usr/bin/env bash
JAR_DIR="\$( cd "\$( dirname "\${0}" )" && pwd )"
# \$1      : Output directory
# \$2...   : List of files to compile
# Example : klangc.sh ./build/gen file1.k file2.k
_outdir="\${1}"
shift
java --enable-preview -jar "\${JAR_DIR}/klangc.jar" --output "\${_outdir}" -- \${@}
EOM
