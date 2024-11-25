/**
 * Tries out:
 *      - combined declaration and assignment
 *      - nested if-else + with empty blocks
 *      - basic ternary
 */
fn max(a: i64, b: i64) -> i64 {
    return a>=b ? a : b;
}

fn max3(a: i64, b: i64, c: i64) -> i64 {
    if max(a,b) >= max(b,c) {
        return max(a,b);
    } 
    // long for "max(b,c)"
    else {
        if b >=c {
            return b;
        }
        else {/*empty statement*/}
        /* NO empty statement */
    }
    if true {/*empty statement*/}
        /* NO empty statement */
    return c;
}

fn ___main___() -> i64 {
    let MIN: i64 = 60;
    let MID: i64;
    MID = 50;
    /*
    return max3(MID, MIN, 71);
    return max3(MID,72,MIN);
    return max3(73,MID,MIN);
    return max3(74,MIN,MID);
    return max3(75,MIN,MID);
    */
    return max3(MIN,76,MID);
}