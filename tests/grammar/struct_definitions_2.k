/**
 * Try out method definitions.
 */

//
struct SomeData {
    field1: i64,
    field2: i64,
    ---
    sum() -> i64 {
        return self.field1 + self.field2;
    }
}
struct SomeData2 {
    field1: i64,
    field2: i64,
    ---
    sum() -> i64 {
        return self.field1 + self.field2;
    }
    sum2() -> i64 {
        return self.field1 + self.field2;
    }
}
struct SomeData3 {
    sum(a:i64, b:i64) -> i64 {
        return a + b;
    }
}
fn staticFun() -> i64 {
        return 42;
}

fn staticFun2(a:i64, b:i64) -> i64 {
        return a+b;
}
