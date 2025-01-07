#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Runs all "test_*.c" files in TEST_FILE_DIR. For each test file first assembly
# code of the corresponding source file in "SRC_FILES_DIR" is generated.
# ==============================================================================

source ./scripts/config.sh
source ./scripts/compile_java.sh

SRC_FILES_DIR=tests
TEST_FILE_DIR=src/test/c/gen_asm

work_dir="${DEV_WORK_DIR}"
C_BIN_DIR="${work_dir}/.c"

# Build project
./scripts/run_antlr.sh
compile_dev
if [ $? -ne 0 ]; then
    echo -e ">>> ERROR while compiling java source files\n"
    exit 1
fi

src_file_compile_dir="${work_dir}/code_gen"

# Iterate over each file in TEST_FILE_DIR with prefix "test_" and extension ".c"
for test_file in "${TEST_FILE_DIR}/test_"*.c; do
    # Extract the file name without extension
    test_file_name_no_ext=$(basename "$test_file" .c)
    src_file_name_no_ext="asm_${test_file_name_no_ext}"

    echo "--- Testfile: '${test_file}'"

    # Compile the test's source file
    echo "Compiling '${SRC_FILES_DIR}/${src_file_name_no_ext}.k' to assembly"
    dependencies_cp=$(_join_array "DEPENDENCIES[@]" ":")
    java --enable-preview -cp "${work_dir}/classes:${dependencies_cp}"\
        cc.crochethk.compilerbau.praktikum.KlangCompiler "${src_file_compile_dir}" "${SRC_FILES_DIR}/${src_file_name_no_ext}.k" > /dev/null
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR. Test skipped!\n"
        continue
    fi

    # Compile the test, linking it with the assembly file using gcc
    echo "Compiling and linking '$test_file' and 'tests.${src_file_name_no_ext}.s'"
    mkdir -p "$C_BIN_DIR"
    gcc -I"${src_file_compile_dir}" -o "${C_BIN_DIR}/${test_file_name_no_ext}"   \
        "${test_file}" "${src_file_compile_dir}/tests.${src_file_name_no_ext}.s" \
        "${src_file_compile_dir}/tests.${src_file_name_no_ext}.c"
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR. Test skipped!\n"
        continue
    fi
    
    # Execute the compiled test binary
    echo "Run test: ${C_BIN_DIR}/${test_file_name_no_ext}"
    "${C_BIN_DIR}/${test_file_name_no_ext}"
    echo ""
done

echo "All tests have been processed."
