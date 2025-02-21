/**
 * Tries out:
 *     - (nested) if-else
 *     - nested returns
 */
fn max(a: i64, b: i64) -> i64 {
    return a >=b ? a : b;
}

fn max3(a: i64, b: i64, c: i64) -> i64 {
    if max(a,b) >= max(b,c) {
        return max(a,b);
    } else {
        if b >=c {
            return b;
        } else {
            return c;
        }
    }
}

fn ___main___() {
    let max: i64;
    /*
    max = max3(50, 60, 71);
    max = max3(50,72,60);
    max = max3(73,50,60);
    max = max3(74,60,50);
    max = max3(75,60,50);
    */
    max = max3(60,76,50);

    print(max); print("\n");
}