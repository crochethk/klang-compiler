
fn _and(lhs: bool, rhs: bool) -> bool {
    return lhs && rhs;
}

fn _or(lhs: bool, rhs: bool) -> bool {
    return lhs || rhs;
}

fn _not(rhs: bool) -> bool {
    return !rhs;
}

fn OFF___main___() {
    print(true);
    print("\n");
    print(false);
    print("\n");

    print(false || !false);
    print("\n");
    print(!(false));
    print("\n");

    print(_not(_not(false)));
    print("\n");
}

