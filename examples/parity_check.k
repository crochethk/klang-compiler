// NOTE: This example also demonstrates how the definition order does not matter.

fn ___main___() {
    let num: i64;

    num = 123;
    print_parity(num);
    print("\n");

    num = 42;
    print_parity(num);
    print("\n");

    num = 0;
    print_parity(num);
    print("\n");

    num = 1;
    print_parity(num);
    print("\n");

    num = 9223372036854775807; // Max. positive 64-bit integer
    print_parity(num);
    print("\n");
}

/**
 * Figures out the parity of the given number using an approach w/ and w/o 
 * modulo operation and prints each result.
 */
fn print_parity(num: i64) {
    let is_odd = is_odd(num);
    print(num); print(" -> ");
    print(getParityString(is_odd)); print("\n");

    is_odd = is_odd_cumbersome(num);
    print(num); print(" -> ");
    print(getParityString(is_odd)); print(" (cumbersome)\n");
}

fn is_odd(number: i64) -> bool {
    return number % 2 != 0;
}

/**
 * Similar to `is_odd` but without using modulo operation.
 */
fn is_odd_cumbersome(number: i64) -> bool {
    return number - (number/2) * 2 != 0;
}

fn getParityString(isOdd: bool) -> string {
    return isOdd ? "odd" : "even";
}

