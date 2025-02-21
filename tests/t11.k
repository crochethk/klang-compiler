/**
 * Tries out:
 *      - (nested) loop and
 *      - break statement
 */

fn ___main___() {
    let sum: i64 = 0;
    let counter: i64 = 0;
    loop {
        if counter < 2 {
            let counter2: i64 = 0;
            loop {
                if counter2 < 21 {
                    counter2 = counter2 + 1;
                    sum = sum +1;
                } else {
                    break;
                }
            }
            counter = counter + 1;
        } else {
            break;
        }
    }
    print(sum); print("\n"); // -> 42
}