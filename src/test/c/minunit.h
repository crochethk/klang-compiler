/* file: minunit.h */
#include <string.h>

#define mu_assert(description, test) do { if (!(test)) return description; } while (0)

#define mu_asserteq(expected, actual) do { if (expected != actual) return #expected " != " #actual; } while (0)

#define mu_assertstreq(expected, actual) do { if (strcmp(expected, actual) != 0) return #expected " != " #actual; } while (0)

/**
 * Runs all tests given as "char* (*tests[])(void)" (array of funptr with return char*)
 * and returns whether all tests were successful.
 */
#define RUN_TESTS(tests) \
    (test_runner(tests, sizeof(tests) / sizeof(tests[0])) < tests_run);

extern int tests_run;

static int test_runner(char* (*tests[])(void), int tests_count) {
    int passed = 0;
    // char* (*tests[])(void) = { __VA_ARGS__ };
    for (int i = 0; i < tests_count; i++) {
        char *message = tests[i](); tests_run++;
        if (message) printf("failed: %s\n", message);
        else passed++;
    }
    if (passed < tests_run) printf("%d TEST(S) FAILED\n", tests_run - passed);
    else printf("ALL TESTS PASSED\n");
    printf("Tests run: %d\n", tests_run);
    return passed;
}

/* Executable test file boilerplate

static char* test_foo() {
    mu_assert("foo != 7", foo == 7);
    return 0;
}
static char* test_bar() {
    mu_asserteq(42, bar);
    return 0;
}

//... TODO add more tests

int tests_run = 0;

int main() {
    char* (*tests[])() = {test_foo, test_bar, test_baz}; // TODO add tests here
    int success = RUN_TESTS(tests);
    return success;
}

*/
