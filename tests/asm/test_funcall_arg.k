/** Test whether nested function calls with arguments are handled properly*/ 

fn ___main___() {
    // print(nestedFunCallWithArgs());
    let num = nestedFunCallWithArgs_1();
    print(num); print("\n"); // should print 42 (NOT 4 or 44)

    num = nestedFunCallWithArgs_2();
    print(num); print("\n"); //->42

    num = nestedFunCallWithArgs_3();
    print(num); print("\n"); //->42

    //---

    print("-----------\n");
    print(reproducedBug()); print("\n"); //->42
    print("-----------\n");
    funCallArgExprWithFunCall_extracted_bug();
}

// return first param
fn fun3_1(a:i64, b:i64, c:i64, ) -> i64 {
    return a;
}
// return second param
fn fun3_2(a:i64, b:i64, c:i64, ) -> i64 {
    return b;
}

// should return 42 (NOT 4 or 44) (first arg)
fn nestedFunCallWithArgs_1() -> i64 {
    return fun3_1(42, fun3_1(4, 5, 6), 3);
}

// should return 42 (result of first arg)
fn nestedFunCallWithArgs_2() -> i64 {
    return fun3_1(fun3_2(1, 42, 3), fun3_1(4, 5, 6), 7);
}

// should return 42 (result of second arg)
fn nestedFunCallWithArgs_3() -> i64 {
    return fun3_2(3, fun3_1(42, 5, 6), 7);
}

//---
fn _div2(a: i64, b:i64) -> i64 {
    return a / b;
}

/*
Before fix caused seg fault, because the format
string arg (pointer) for the underlying 'print@PLT'
call was put in rdi too early and then got
overwritten by "2772". 'print@PLT' then tries to
interpret the memory address "$2772" as the 
pointer to a format string (which is not valid).
*/
fn funCallArgExprWithFunCall_extracted_bug() {
    print(_div2(2772, 22) / 3); //-> 42
    print("\n");
}

/*
Same bug but without seg fault. Instead the wrong result
is produced (because register holding arg is overwritten).
*/
fn reproducedBug() -> i64 {
    return return_a(42, return_a(-122, 456)+1); // -> 42
}
fn return_a(a:i64, b: i64) -> i64{
    return a;
}
