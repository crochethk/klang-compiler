
// Meant for manual verification of register management during FunCalls
/**
 *    xmm_ args: 9x f64     -> 8 xmm
 * regular args: 7x i64     -> 6 regs
 */
fn f16_1(
    x1: i64,
    d1: f64,
    x2: i64,
    d2: f64,
    x3: i64,
    d3: f64,
    x4: i64,
    d4: f64,
    d5: f64,
    d6: f64,
    x5: i64,
    d7: f64,
    x6: i64,
    d8: f64,
    d9: f64,
    x7: i64
    ) -> i64 {
    return x7;
}

/**
 *   float args:  6x f64    -> 6 xmm
 * regular args: 10x i64    -> 6 regs
 */
fn f16_2(
    x1: i64,
    d1: i64,
    x2: i64,
    d2: i64,
    x3: i64,
    d3: i64,
    x4: i64,
    d4: f64,
    d5: f64,
    d6: f64,
    x5: i64,
    d7: f64,
    x6: i64,
    d8: f64,
    d9: f64,
    x7: i64
    ) -> i64 {
    return x7;
}

/**
 *   float args: 13x f64    -> 8 xmm
 * regular args:  3x i64    -> 3 regs
 */
fn f16_3(
    x1: f64,
    d1: f64,
    x2: f64,
    d2: f64,
    x3: f64,
    d3: f64,
    x4: f64,
    d4: f64,
    d5: f64,
    d6: f64,
    x5: i64,
    d7: f64,
    x6: i64,
    d8: f64,
    d9: f64,
    x7: i64
    ) -> i64 {
    return x7;
}

fn ___main___() {
    let x7 = f16_1(
        1,   2.0,  3,  4.0,    5, 6.0, 7, 8.0, 9.0, 10.0, 
        11, 12.0, 13, 14.0, 15.0,  16);
    // x7 = f16_2(1,2,3,4,5,6,7,8.0,9.0,10.0,11,12.0,13,14.0,15.0,16);
    // x7 = f16_3(1.01,1.2,1.3,1.4,1.5,1.6,1.7,1.8,1.9,10.0,11,12.0,13,14.0,15.0,16);
}
