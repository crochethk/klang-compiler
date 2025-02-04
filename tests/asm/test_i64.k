/* Empty function */
fn f1() {}

/* Return integer */
fn get_42() -> i64 {
    return 42 as i64;
}

/* More than 6 params */
fn soManyParams(x1:i64, x2:i64, x3:i64, x4:i64, x5:i64, x6:i64, x7:i64) -> i64 {
    return x1+x2+x3+x4+x5+x6+x7;
}

/* More than 6 and mixed params */
fn soManyMixedParams(x1:i64, b1:bool, x2:i64, x3:i64, x4:i64, x5:i64, x6:i64, b2:bool, x7:i64) -> i64 {
    return x1+x2+x3+x4+x5+x6+x7;
}

/* Declaration */
fn decl() -> i64 {
    let foo: i64;
    return 1; 
}

/* Declaration + Assignment */
fn declAssign() -> i64 {
    let foo: i64 = 21;
    return foo + foo; 
}

/* FunCall */
fn withFunCall() -> i64{
    return soManyParams(get_42(), 2,3,4,5,6,7);
}

/* Sub */
fn longSub(lhs:i64, rhs: i64) -> i64 {
    return lhs - rhs;
}

/* Sub */
fn longSub7(x1:i64, x2:i64, x3:i64, x4:i64, x5:i64, x6:i64, x7:i64) -> i64 {
    return x1-x2-x3-x4-x5-x6-x7;
}

/* Sub */
fn longSubWithFunCall_1(rhs: i64) -> i64 {
    return get_42()-rhs ;
}

/* Sub */
fn longSubWithFunCall_2(lhs: i64) -> i64 {
    return lhs-get_42() ;
}

/* Mul */
fn longMul(lhs:i64, rhs: i64) -> i64 {
    return lhs*rhs ;
}
/* Mul, Add */
fn longMulAndAdd_1(lhs:i64, rhs: i64) -> i64 {
    return lhs*rhs+84;
}

/* Add, Mul */
fn longMulAndAdd_2(lhs:i64, rhs: i64) -> i64 {
    return 84+lhs*rhs;
}

/* Simple div */
fn _div2(a: i64, b:i64) -> i64 {
    return a / b;
}

/* Chained divs */
fn _div4(a: i64, b:i64, c:i64, d:i64) -> i64 {
    return a / b / c / d;
}
/**
Test partial expr, which isolated would eval to 0 and lead to "divide by zero"
(when there should no occur).
*/
fn divChained_2() -> i64 {
    // isolated "17/21" is 0. should not in context though.
    return 123456 / 3 / 17 / 21; //-> 115
}

fn divChained_3() -> i64 {
    // isolated "17/21" is 0. should not in context though.
    return 123456 / 17 / 21 / 3; //-> 115
}

fn divChainWithFunCall_return_8230() -> i64 {
    return _div2(123456, 3)/5; //-> 115
}

//-------------

/* Mod */
fn longMod(lhs:i64, rhs: i64) -> i64 {
    return lhs%rhs;
}

/* Mod, Add
- should first calculate modulo, then add 84
*/
fn longModAndAdd_1(lhs:i64, rhs: i64) -> i64 {
    return lhs%rhs+84;
}

/* Add, Mod
- should first calculate modulo, then add 84
*/
fn longModAndAdd_2(lhs:i64, rhs: i64) -> i64 {
    return 84+lhs%rhs;
}
//-----------------

fn OFF___main___() {

    print("Expect: 4\n");
    print("Actual: "); print(_div2(8,2)); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(_div2(2,8)); print("\n\n");

    print("Expect: 115272\n");
    print("Actual: "); print(_div4(123456789,3,17,21)); print("\n\n");

    print("Expect: -115272\n");
    print("Actual: "); print(_div4(-123456789,3,17,21)); print("\n\n");

    print("Expect: -115272\n");
    print("Actual: "); print(_div4(123456789,-3,17,21)); print("\n\n");

    print("Expect: -115272\n");
    print("Actual: "); print(_div4(123456789,-3,-17,-21)); print("\n\n");

    print("Expect: 115272\n");
    print("Actual: "); print(_div4(123456789,3,17,21)); print("\n\n");

    print("Expect: 8230\n");
    print("Actual: "); print(divChainWithFunCall_return_8230()); print("\n\n");
}

//-------------

/* Mod */
fn longMod(lhs:i64, rhs: i64) -> i64 {
    return lhs%rhs;
}

/* Mod, Add
- should first calculate modulo, then add 84
*/
fn longModAndAdd_1(lhs:i64, rhs: i64) -> i64 {
    return lhs%rhs+84;
}

/* Add, Mod
- should first calculate modulo, then add 84
*/
fn longModAndAdd_2(lhs:i64, rhs: i64) -> i64 {
    return 84+lhs%rhs;
}
