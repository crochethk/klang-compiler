/**
 * Tries out:
 *      - double support
 */
fn max(a: f64, b: f64) -> f64 {
    return a>=b ? a : b;
}

fn max3(a: f64, b: f64, c: f64) -> f64 {
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

fn ___main___() -> f64 {
    let MIN: f64 = 50.0;
    let MID: f64 = 60.0;
    let MAX: f64;
    MAX = 70.0+-42.1+42.1 as f64*13 as f64/13 as f64;
    /*
    return max3(MID, MIN, MAX+1.0);
    return max3(MID,MAX+2.0,MIN);
    return max3(MAX+3.0,MID,MIN);
    return max3(MAX+4.0,MIN,MID);
    return max3(MAX+5.0,MIN,MID);
    */
    return max3(MIN,MAX+6.0,MID);
}