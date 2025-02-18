//
// Test field setter
//

fn modifySimpleField(obj: OneField, newVal: i64) { 
    obj.num = newVal;
}

fn modifyNestedField(obj: Nested, newVal: i64) {
    obj.oneFielder.num = newVal;
}

fn modifyMultiPrimFields(obj: MultiPrimFields, theInt: i64, theBool: bool, theFloat: f64) { 
    obj.aInt = theInt;
    obj.aFloat = theFloat;
    obj.aBool = theBool;
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
