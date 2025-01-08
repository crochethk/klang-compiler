#include <stdio.h>
#include <stdbool.h>

#include "../minunit.h"
#include "tests.asm.test_struct_helpers_1.h"

char* test_tostring1() {
    struct Empty st;
    char* actual = tests$$asm$test_struct_helpers_1$Empty$to_string(&st);
    printf("%s\n", actual);
    mu_assertstreq("Empty()", actual);
    return 0;
}

char* test_tostring2() {
    struct MyStruct st;
    st.hello = 0;
    st.world = "zero";
    char* actual = tests$$asm$test_struct_helpers_1$MyStruct$to_string(&st);
    printf("%s\n", actual);
    mu_assertstreq("MyStruct(0, zero)", actual);
    return 0;
}

// use constructor and tostring
char* test_tostring3() {
    struct MyStruct* st = tests$$asm$test_struct_helpers_1$MyStruct$new(42, "fourtytwo");
    char* actual = tests$$asm$test_struct_helpers_1$MyStruct$to_string(st);
    printf("%s\n", actual);
    mu_assertstreq("MyStruct(42, fourtytwo)", actual);
    tests$$asm$test_struct_helpers_1$MyStruct$dispose(st);
    return 0;
}

// struct with struct field
char* test_tostring4() {
    struct MyStruct* fieldstruct = tests$$asm$test_struct_helpers_1$MyStruct$new(11, "one-one");
    struct UseOtherStruct* st = tests$$asm$test_struct_helpers_1$UseOtherStruct$new(1, "one", fieldstruct);
    char* actual = tests$$asm$test_struct_helpers_1$UseOtherStruct$to_string(st);
    printf("%s\n", actual);
    mu_assertstreq("UseOtherStruct(1, one, MyStruct(11, one-one))", actual);
    tests$$asm$test_struct_helpers_1$MyStruct$dispose(fieldstruct);
    tests$$asm$test_struct_helpers_1$UseOtherStruct$dispose(st);
    return 0;
}

int tests_run = 0;
int main() {
    char* (*tests[])() = {
        test_tostring1
        , test_tostring2
        , test_tostring3
        , test_tostring4

    };
    return RUN_TESTS(tests);
}
