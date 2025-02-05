#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Builds a self-contained JAR of the klang compiler.
#
# Jar Usage see: java --enable-preview -jar klangc.jar --help
# ==============================================================================

source ./scripts/config.sh
source ./scripts/compile_java.sh

work_dir="${JAR_WORK_DIR}"
classes_out_dir="${work_dir}/classes/"

# Compile compiler and copy to workdir
compile_release
if [ $? -ne 0 ]; then
    echo -e "\e[31m>>> ERROR while compiling java source files\e[0m\n"
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
jar --create --file "${jar_path}" --main-class cc.crochethk.klang.KlangCompiler -C "${classes_out_dir}" .
echo "Jar created: '${jar_path}'"

# Create helper script to execute jar
jar_runner="${BUILD_ARTIFACTS_BASE_DIR}/klangc.sh"

cat > "${jar_runner}" <<- EOM
#!/usr/bin/env bash
# Use "--help" for usage info.
jar_dir="\$( cd "\$( dirname "\${0}" )" && pwd )"
java --enable-preview -jar "\${jar_dir}/klangc.jar" \${@}
EOM
