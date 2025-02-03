/* Empty function */
fn f1() {}

/* Return integer */
fn f42() -> i64 {
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
    return soManyParams(f42(), 2,3,4,5,6,7);
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
    return f42()-rhs ;
}

/* Sub */
fn longSubWithFunCall_2(lhs: i64) -> i64 {
    return lhs-f42() ;
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

// -----
// TODO add variants for Div

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


