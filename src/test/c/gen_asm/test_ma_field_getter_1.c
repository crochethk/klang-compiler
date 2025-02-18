#include <stdbool.h>
#include <stdio.h>

#include "../minunit.h"
#include "tests.asm.test_ma_field_getter_1.h"

char* test_get_simple_field() {
    int64_t num = accessSimpleField();
    mu_asserteq(123, num);
    return 0;
}

char* test_get_field_from_nested_struct() {
    int64_t num = accessNestedField();
    mu_asserteq(456, num);
    return 0;
}

int tests_run = 0;
int main() {
    char* (*tests[])() = {
        test_get_simple_field,
        test_get_field_from_nested_struct  //
    };
    return RUN_TESTS(tests);
}
