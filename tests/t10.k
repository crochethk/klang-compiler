/**
 * Tries out:
 *      - type annotations
 *      - implicit void returns
 */
fn emptyFun() {}

fn ___main___() -> void {
    let n4: i64 = 123 as i64;
    // let n5: i64 = 123.2 as i64; // -> should fail rightnow
    let f4: f64 = 123 as f64;
    let f5: f64 = 123.2 as f64;
}