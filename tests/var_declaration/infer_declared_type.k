fn infer_i64() {
    let myvar = 123;
    myvar = myvar + 456;
}

fn infer_f64() {
    let myvar = 1.23;
    print("myvar: ");
    print(myvar);
    print("\n");

    myvar = myvar + 4.56;
    print("myvar: ");
    print(myvar);
    print("\n");
}

fn infer_string() {
    let myvar = "hello";
    print("myvar: ");
    print(myvar);
    print("\n");
}

fn ___main___() {
    infer_f64();
    infer_string();
}