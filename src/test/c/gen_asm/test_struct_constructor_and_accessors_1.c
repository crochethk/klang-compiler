#include <stdio.h>
#include <stdbool.h>

#include "../minunit.h"
#include "tests.asm.test_struct_constructor_and_accessors_1.h"

typedef struct NumWithText NumWithText;

char* test_getters() {
    NumWithText* numWtxt = createNumWithText();
    int64_t num = getTheNumberField(numWtxt);
    mu_asserteq(42, num);

    char* txt = getTheTextField(numWtxt);
    mu_assertstreq("fourtytwo", txt);

    free(numWtxt);
    return 0;
}

char* test_setters() {
    NumWithText* numWtxt = createNumWithText();
    changeTo43(numWtxt);

    int64_t num = getTheNumberField(numWtxt);
    mu_asserteq(43, num);
    char* txt = getTheTextField(numWtxt);
    mu_assertstreq("fourtythree", txt);

    free(numWtxt);
    return 0;
}

int tests_run = 0;
int main() {
    char* (*tests[])() = {
        test_getters
        , test_setters
    };
    return RUN_TESTS(tests);
}
