#!/bin/sh

# ==============================================================================
# Runs all "test_*.c" files in TEST_FILE_DIR. For each test file first assembly
# code of the corresponding source file in "SRC_FILES_DIR" is generated.
# ==============================================================================

# Constants
C_BIN_DIR=bin/.c
TEST_FILE_DIR=src/test/c/gen_asm
SRC_FILES_DIR=tests
SRC_FILE_COMPILE_DIR=code_gen

mkdir -p $C_BIN_DIR

# Iterate over each file in TEST_FILE_DIR with prefix "test_" and extension ".c"
for test_file in $TEST_FILE_DIR/test_*.c; do
    # Extract the file name without extension
    test_file_name_no_ext=$(basename $test_file .c)
    src_file_name_no_ext=asm_$test_file_name_no_ext

    echo "--- Testfile: '$test_file'"

    # Compile the test's source file
    echo "Compiling '${SRC_FILES_DIR}/${src_file_name_no_ext}.L1' to assembly"
    java --enable-preview -cp "bin:src/main/java:src/main/gen:src/test/java:code_gen:lib/*:"\
        cc.crochethk.compilerbau.praktikum.L1Compiler $SRC_FILE_COMPILE_DIR $SRC_FILES_DIR/${src_file_name_no_ext}.L1 > /dev/null
    if [ $? -ne 0 ]; then
        echo ">>> ERROR. Test skipped!\n"
        continue
    fi

    # Compile the test, linking it with the assembly file using gcc
    echo "Compiling and linking '$test_file' and 'tests.${src_file_name_no_ext}.s'"
    gcc -o $C_BIN_DIR/$test_file_name_no_ext $test_file $SRC_FILE_COMPILE_DIR/tests.${src_file_name_no_ext}.s
    if [ $? -ne 0 ]; then
        echo ">>> ERROR. Test skipped!\n"
        continue
    fi
    
    # Execute the compiled test binary
    echo "Run test: $C_BIN_DIR/$test_file_name_no_ext"
    $C_BIN_DIR/$test_file_name_no_ext
    echo ""
done

echo "All tests have been processed."
