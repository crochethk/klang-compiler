/**
 * Test optimization:
 *  - literal constant labels should be reused for similar values
 *
 *  ===> no duplicate .LC{n} sections should be produced in assembly file
 */

fn use_equal_lit_float_multiple_times() -> void {
    let x0 = 42.0;
    let x1 = 42.0;
    let x2 = 42.0;
    let x3 = 42.0;
    let x4 = 42.0;
    print(x0); print("\n");
    print(x1); print("\n");
    print(x2); print("\n");
    print(x3); print("\n");
    print(x4); print("\n");
}

fn use_equal_lit_str_multiple_times() -> void {
    let x0 = "here's some string literal";
    let x1 = "here's some string literal";
    let x2 = "here's some string literal";
    let x3 = "here's some string literal";
    let x4 = "here's some string literal";
    print(x0); print("\n");
    print(x1); print("\n");
    print(x2); print("\n");
    print(x3); print("\n");
    print(x4); print("\n");
}

fn ___main___() {
    use_equal_lit_float_multiple_times();
    use_equal_lit_str_multiple_times();
}
