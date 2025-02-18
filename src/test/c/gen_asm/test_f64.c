#include <stdio.h>
#include <stdbool.h>

#include "../minunit.h"
#include "tests.asm.test_f64.h"

char* test_doubleConst42() {
    mu_asserteq(42., doubleConst42());
    return 0;
}

// TODO use the functions in "test_f64.k"...

int tests_run = 0;
int main() {
    char* (*tests[])() = {
        test_doubleConst42 //
    };
    return RUN_TESTS(tests);
}
