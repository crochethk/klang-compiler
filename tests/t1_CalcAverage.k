
// Calculates average of two integer values
fn average(a: i64, b:i64, dummy_bool: bool /* this is only a dummy parameter to test bool parsing and typecheck*/) -> i64 {
    return (
        a+b
    ) /2;
}
fn ___main___() -> i64 {
    return average(14, 70, false); // -> 42
}