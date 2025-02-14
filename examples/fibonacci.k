fn fib_rec(n: i64) -> i64 {
    return n <= 1 ? n : fib_rec(n-1) + fib_rec(n-2);
}

fn fib_iter(n: i64) -> i64 {
    if n <= 1 {
        return n;
    }

    let a = 0;
    let b = 1;
    let result = 0;

    let i = 2;
    loop {
        if i > n {
            break;
        }
        result = a + b;
        a = b;
        b = result;
        i = i + 1;
    }
    return result;
}

fn ___main___() {
    print(fib_rec(7)); print("\n"); // -> 13
    print(fib_iter(7)); print("\n"); // -> 13

    print(fib_rec(41)); print("\n"); // -> 165580141
    print(fib_iter(41)); print("\n"); // -> 165580141
}
