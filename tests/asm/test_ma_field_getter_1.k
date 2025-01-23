//
// Test field getter
//

fn buildTestStruct() -> Nested {
    return Nested { OneField{456} };
}

fn accessNestedField() -> i64 {
    let nested: Nested = buildTestStruct();
    return nested.oneFielder.num;
}

fn accessSimpleField() -> i64 { let oneFielder: OneField = OneField{123}; return oneFielder.num; }

struct OneField {
    num: i64,
}

struct Nested {
    oneFielder: OneField,
}
