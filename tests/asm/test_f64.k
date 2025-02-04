/* return double constant */
fn doubleConst42() -> f64 {
    return 42.0;
}

/* simple add */
fn _add2(a: f64, b:f64) -> f64 {
    return a + b;
}

/* chained add */
fn _add4_1(a: f64, b:f64, c:f64, d:f64) -> f64 {
    return a + b + c + d;
}

/* add4 decoupled  */
fn _add4_2(a: f64, b:f64, c:f64, d:f64) -> f64 {
    let r1 = a+b;
    let r2 = c+d;
    return r1+r2;
}


fn _div4_1(a: f64, b:f64, c:f64, d:f64) -> f64 {
    return a / b / c / d;
}

//------------

fn gt(a: f64, b: f64) -> bool {
    return a > b;
}
fn gte(a: f64, b: f64) -> bool {
    return a >= b;
}
fn lt(a: f64, b: f64) -> bool {
    return a < b;
}
fn lte(a: f64, b: f64) -> bool {
    return a <= b;
}
fn eq(a: f64, b: f64) -> bool {
    return a == b;
}
fn ne(a: f64, b: f64) -> bool {
    return a != b;
}

//-----------------

fn OFF___main___() {
    print("Expect: 4.000000\n");
    print("Actual: "); print(_add2(1.0,3.0)); print("\n\n");

    print("Expect: 13.000000\n");
    print("Actual: "); print(_add4_1(1.0,3.0,4.0,5.0)); print("\n\n");

    print("Expect: 13.000000\n");
    print("Actual: "); print(_add4_2(1.0,3.0,4.0,5.0)); print("\n\n");

    print("Expect: 7.000000\n");
    print("Actual: "); print(_add4_1(1.0,-3.0,4.0,5.0)); print("\n\n");

    print("Expect: 7.000000\n");
    print("Actual: "); print(_add4_2(1.0,-3.0,4.0,5.0)); print("\n\n");

    print("Expect: -0.208333\n");
    print("Actual: "); print(_div4_1(5.0,-4.0,3.0,2.0)); print("\n\n");

    //-------------

    print("Expect: 1\n");
    print("Actual: "); print(12.3==12.3); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(12.3==12.4); print("\n\n");

    print("Expect: 1\n");
    print("Actual: "); print(12.3!=12.4); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(12.3!=12.3); print("\n\n");

    print("Expect: 1\n");
    print("Actual: "); print(12.3<45.6); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(12.3<4.5); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(4.5<4.5); print("\n\n");

    print("Expect: 1\n");
    print("Actual: "); print(12.3>=4.5); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(12.3>=45.6); print("\n\n");
    print("Expect: 1\n");
    print("Actual: "); print(12.3>=12.3); print("\n\n");

    print("Expect: 1\n");
    print("Actual: "); print(45.6>12.3); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(12.3>45.6); print("\n\n");

    print("Expect: 1\n");
    print("Actual: "); print(12.3<=45.6); print("\n\n");
    print("Expect: 0\n");
    print("Actual: "); print(12.3<=4.5); print("\n\n");
    print("Expect: 1\n");
    print("Actual: "); print(12.3<=12.3); print("\n\n");
}
