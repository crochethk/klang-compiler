/**
 * Tries out new grammar structure:
 *      - multi statement (statement list)
 *      - empty function
 *      - block statements
 */
fn foo() -> bool {
    // return b;
    // a= 123+45/678-1;
    // {
    //     varInsideExtraBlock = 0;
    // }
    let a: bool = true;
    let b: bool = false;
    // return d;
    if a {
        return true;
    }
    if b {
        return false;
    }
    else {
        return !a && !b;
    }
}

fn bar() ->void{}
