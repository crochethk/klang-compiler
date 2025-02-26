//
// Test field getter
//

fn buildTestStruct() -> Nested {
    return Nested { OneField{456} };
}
fn dropTestStruct(o: Nested) {
    let field = o.oneFielder;
    drop field;
    drop o;
}

fn accessNestedField() -> i64 {
    let nested: Nested = buildTestStruct();
    let result = nested.oneFielder.num;
    dropTestStruct(nested);
    return result;
}

fn accessSimpleField() -> i64 { 
    let oneFielder: OneField = OneField{123};
    let result = oneFielder.num;
    drop oneFielder;
    return result;
}

struct OneField {
    num: i64,
}

struct Nested {
    oneFielder: OneField,
}
