/**
 * Tries out:
 *     - multi-statement
 *     - declaration
 *     - assignment
 */
fn sum_odd_from_to(acc: i64, begin:i64, end:i64) -> i64 {
    if begin > end {  // Base case: stop when current exceeds target
        return acc;
    } else {
        if is_odd(begin) {
            return sum_odd_from_to(acc + begin, begin+1, end);
        }
        else {
            return sum_odd_from_to(acc, begin+1, end);
        }
    }
}

fn is_odd(number: i64) -> bool {
    // return number % 2 != 0;
    return number - (number/2) * 2 != 0;
}

fn ___main___() {
    let the_start :i64;
    let the_end :i64;
    the_start = 1;
    the_end = 15;
    let result = sum_odd_from_to(0, the_start, the_end);
    print(result); print("\n"); // -> 64
}