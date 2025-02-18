#include <stdbool.h>
#include <stdio.h>

#include "../minunit.h"
#include "tests.asm.test_ma_field_setter_1.h"

typedef struct OneField OneField;
typedef struct Nested Nested;
typedef struct MultiPrimFields MultiPrimFields;

char* test_set_simple_field() {
    OneField* obj = OneField$new$(123);
    mu_asserteq(123, obj->num);

    modifySimpleField(obj, 42);
    mu_asserteq(42, obj->num);

    free(obj);
    return 0;
}

char* test_set_nested_field() {
    OneField* oneFielder = OneField$new$(123);
    Nested* obj = Nested$new$(oneFielder);
    mu_asserteq(123, obj->oneFielder->num);

    modifyNestedField(obj, 42);
    mu_asserteq(42, obj->oneFielder->num);

    free(oneFielder);
    free(obj);
    return 0;
}

char* test_set_multiprimfields_fields() {
    MultiPrimFields* obj = MultiPrimFields$new$(false, 789, 6.54);
    mu_asserteq(false, obj->aBool);
    mu_asserteq(789, obj->aInt);
    mu_asserteq(6.54, obj->aFloat);

    modifyMultiPrimFields(obj, 4242, true, 1.23);
    mu_asserteq(true, obj->aBool);
    mu_asserteq(4242, obj->aInt);
    mu_asserteq(1.23, obj->aFloat);

    free(obj);
    return 0;
}

int tests_run = 0;
int main() {
    char* (*tests[])() = {
        test_set_simple_field,
        test_set_nested_field,           //
        test_set_multiprimfields_fields  //
    };
    return RUN_TESTS(tests);
}
