#!/usr/bin/env bash
# >>> Expects $(pwd) to be the __project root folder__. <<<

# ==============================================================================
# Runs all "test_*.c" files in TEST_FILE_DIR. For each test file first assembly
# code of the corresponding source file in "SRC_FILES_DIR" is generated.
# ==============================================================================

# Constants
JAVA_BIN_DIR=./build/dev
C_BIN_DIR=build/dev/.c
TEST_FILE_DIR=src/test/c/gen_asm
SRC_FILES_DIR=tests

SRC_FILE_COMPILE_DIR=build/dev/code_gen

# make sure lexer/parser were generated
$(dirname $0)/run_antlr.sh

mkdir -p $C_BIN_DIR

# compile all java source files
sourcesListFile=${JAVA_BIN_DIR}/sources.txt

find "./src" -type f -name "*.java" > ${sourcesListFile}
javac --enable-preview --source 23 --target 23 -cp "lib/*:" -d "${JAVA_BIN_DIR}" @"${sourcesListFile}"

if [ $? -ne 0 ]; then
    echo -e ">>> ERROR while compiling java source files\n"
    exit 1
fi


# Iterate over each file in TEST_FILE_DIR with prefix "test_" and extension ".c"
for test_file in $TEST_FILE_DIR/test_*.c; do
    # Extract the file name without extension
    test_file_name_no_ext=$(basename $test_file .c)
    src_file_name_no_ext=asm_$test_file_name_no_ext

    echo "--- Testfile: '$test_file'"

    # Compile the test's source file
    echo "Compiling '${SRC_FILES_DIR}/${src_file_name_no_ext}.k' to assembly"
    java --enable-preview -cp "build/dev:lib/*:"\
        cc.crochethk.compilerbau.praktikum.KlangCompiler $SRC_FILE_COMPILE_DIR $SRC_FILES_DIR/${src_file_name_no_ext}.k > /dev/null
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR. Test skipped!\n"
        continue
    fi

    # Compile the test, linking it with the assembly file using gcc
    echo "Compiling and linking '$test_file' and 'tests.${src_file_name_no_ext}.s'"
    gcc -o $C_BIN_DIR/$test_file_name_no_ext $test_file $SRC_FILE_COMPILE_DIR/tests.${src_file_name_no_ext}.s
    if [ $? -ne 0 ]; then
        echo -e ">>> ERROR. Test skipped!\n"
        continue
    fi
    
    # Execute the compiled test binary
    echo "Run test: $C_BIN_DIR/$test_file_name_no_ext"
    $C_BIN_DIR/$test_file_name_no_ext
    echo ""
done

echo "All tests have been processed."
