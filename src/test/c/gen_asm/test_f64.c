#include <stdio.h>
#include <stdbool.h>

#include "../minunit.h"
#include "tests.asm.test_f64.h"

char* test_doubleConst42() {
    mu_asserteq(42.d, doubleConst42());
    return 0;
}

// TODO use float/double functions...
// char* test_soManyParams() { mu_asserteq(28, soManyParams(1,2,3,4,5,6,7)); return 0; }
// char* test_withFunCall() { mu_asserteq(69, withFunCall()); return 0; }
// char* test_soManyMixedParams() { mu_asserteq(28, soManyMixedParams(1,true,2,3,4,5,6,false,7)); return 0;}
// char* test_decl() { mu_asserteq(1, decl()); return 0;}
// char* test_declAssign() { mu_asserteq(42, declAssign()); return 0;}
// char* test_longSub() { mu_asserteq(42, longSub(44,2)); return 0;}
// char* test_longSub7() { mu_asserteq(-26, longSub7(1,2,3,4,5,6,7)); return 0;}
// char* test_longSubWithFunCall_1() { mu_asserteq(-42, longSubWithFunCall_1(84)); return 0;}
// char* test_longSubWithFunCall_2() { mu_asserteq(42, longSubWithFunCall_2(84)); return 0;}
// char* test_longMul() { mu_asserteq(42, longMul(3,14)); return 0;}
// char* test_longMulAndAdd_1() { mu_asserteq(42, longMulAndAdd_1(-3,14)); return 0;}
// char* test_longMulAndAdd_2() { mu_asserteq(42, longMulAndAdd_2(-3,14)); return 0;}

int tests_run = 0;
int main() {
    char* (*tests[])() = {
        test_doubleConst42
        //,
        // test_soManyParams,
        // test_withFunCall,
        // test_soManyMixedParams,
        // test_decl,
        // test_declAssign,
        // test_longSub,
        // test_longSub7,
        // test_longSubWithFunCall_1,
        // test_longSubWithFunCall_2,
        // test_longMul,
        // test_longMulAndAdd_1,
        // test_longMulAndAdd_2
    };
    return RUN_TESTS(tests);
}
