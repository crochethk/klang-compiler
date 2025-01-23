//
// Test field setter
//

fn modifyNestedField(obj: Nested, newVal: i64) {
    obj.oneFielder.num = newVal;
}

fn modifySimpleField(obj: OneField, newVal: i64) { 
    obj.num = newVal;
}

fn modifyMultiPrimFields(obj: MultiPrimFields, aInt: i64) { 
    obj.aInt = aInt;
}

struct OneField {
    num: i64,
}

struct Nested {
    oneFielder: OneField,
}

struct MultiPrimFields {
    aBool: bool,
    aInt: i64,
    aFloat: f64,
}
