/**
 * Tries out:
 *     - funcall
 *     - bool result
 *     - neg operator
 *     - comparison operator
 */
fn is_odd(number: i64) -> bool {
    // return number % 2 != 0;
    // return !(number - (number/2) * 2 == 0);
    return number - (number/2) * 2 != 0;
}

fn ___main___() {
    print(is_odd(2147483647)); print("\n");
}