fn ___main___() {
    let myint: i64;
    let myfloat: f64;

    myint = 123;
    print("myint to float: "); 
    print(myint); print(" -> "); print(myint as f64);
    print("\n\n");

    myfloat = 4.32;
    print("myfloat to integer: "); 
    print(myfloat); print(" -> "); print(myfloat as i64);
    print("\n\n");

    myint = -123;
    print("myint to float: "); 
    print(myint); print(" -> "); print(myint as f64);
    print("\n\n");

    myfloat = -4.32;
    print("myfloat to integer: "); 
    print(myfloat); print(" -> "); print(myfloat as i64);
    print("\n\n");


    let intExpr: i64;
    let floatExpr: f64;

    intExpr = 23.45 as i64 + 19;
    print(intExpr); print("\n\n"); // Expected: 42

    floatExpr = 4 as f64 + 0.2;
    print(floatExpr); print("\n\n"); // Expected: 4.200000
}