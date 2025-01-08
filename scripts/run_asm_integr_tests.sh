#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Without parameters runs all "test_" prefixed C files in TEST_ASM_TESTS_DIR. For each 
# test file first assembly code of the corresponding source file in
# "TEST_KLANG_FILES_DIR" is generated.
#
# Optional Parameters:
#   $1 : Filename pattern of test runner files to execute. This is useful to run
#           a single test or all tests of a specific name pattern.
#        Example:
#           "test_case_X_*" will run all C files in TEST_ASM_TESTS_DIR prefixed
#           with "test_case_X_".
# ==============================================================================

source ./scripts/config.sh
source ./scripts/compile_java.sh
source ./scripts/compile_klang.sh

# Compile compiler
compile_release
if [ $? -ne 0 ]; then
    echo -e ">>> ERROR while compiling java source files\n"
    exit 1
fi

work_dir="${TEST_WORK_DIR}/asm"
src_file_compile_dir="${work_dir}/code_gen"
c_bin_dir="${work_dir}/bin"
mkdir -p "$c_bin_dir"

if [[ $# -eq 1 ]]; then
    file_filter="${1}.c"
else
    file_filter="test_*.c"
fi
file_list=$( find "${TEST_ASM_TESTS_DIR}" -type f -name "${file_filter}" )

# Workaround (pt1/2) for potential " " in filenames
old_IFS="$IFS"
IFS=$'\n'

# Iterate over each file in TEST_ASM_TESTS_DIR with prefix "test_" and extension ".c"
for test_file in ${file_list}; do
    # Extract the file name without extension
    test_file_name_no_ext=$(basename "$test_file" .c)
    src_file_name_no_ext="asm_${test_file_name_no_ext}"

    echo "+-------------------------------------------------------------------------------"
    echo "+     Test: '${test_file}'"
    echo "+-------------------------------------------------------------------------------"

    # Compile the test's corresponding source file
    echo "Compiling '${TEST_KLANG_FILES_DIR}/${src_file_name_no_ext}.k' to assembly"
    dependencies_cp=$(_join_array "DEPENDENCIES[@]" ":")
    compile_klang_files "${src_file_compile_dir}" "${TEST_KLANG_FILES_DIR}/${src_file_name_no_ext}.k"
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR. Test skipped!\n"
        continue
    fi

    # Compile the test, linking it with the assembly file using gcc
    echo "Compiling and linking '$test_file' and 'tests.${src_file_name_no_ext}.s'"

    gcc -I"${src_file_compile_dir}" -o "${c_bin_dir}/${test_file_name_no_ext}"   \
        "${test_file}" "${src_file_compile_dir}/tests.${src_file_name_no_ext}.s" \
        "${src_file_compile_dir}/tests.${src_file_name_no_ext}.c"
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR. Test skipped!\n"
        continue
    fi

    # Execute the compiled test binary
    echo "Run test: ${c_bin_dir}/${test_file_name_no_ext}"
    "${c_bin_dir}/${test_file_name_no_ext}"
    echo ""
done
# Workaround (pt2/2)
IFS="$old_IFS"

echo "All tests have been processed."
