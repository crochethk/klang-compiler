/**
 * Tries out:
 *      - fibonacci
 */

fn fib(n: i64) -> i64 {
    return n<=1 ? n : fib(n-1)+fib(n-2);
    // if n<=1 {return n;}
    // else {
    //     return fib(n-1)+fib(n-2);
    // }
}

fn ___main___() {
    print(fib(7)); print("\n"); // -> 13
    print(floatingFib(7.0)); print("\n"); // -> 13.0
}

fn floatingFib(n: f64) -> f64 {
    return n<=1.0 ? n : floatingFib(n-1.0)+floatingFib(n-2.0);
}

