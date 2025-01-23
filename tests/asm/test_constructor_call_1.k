//
// Test constructors
//

// ----------[ Struct Defs ]----------

struct Empty {}

struct OnePrimField {
    aInt: i64,
}

struct OneCustomRefTypeField {
    struct_field: OnePrimField,
}

struct OneStringField {
    string_field: string,
}

struct MultiPrimFields {
    aInt: i64,
    aBool: bool,
    aFloat: f64,
}

struct PrimAndRefFields {
    aBool: bool,
    aStr: string,
    aStruct: OneCustomRefTypeField,
}

// ----------[ funs ]----------

fn buildEmpty() -> Empty {
    return Empty{};
}

fn buildOnePrimField() -> OnePrimField {
    return OnePrimField{42};
}

fn buildOneCustomRefTypeField() -> OneCustomRefTypeField {
    return OneCustomRefTypeField{
        buildOnePrimField()
    };
}

fn buildOneStringField() -> OneStringField {
    return OneStringField{"foo"};
}

fn buildMultiPrimFields() -> MultiPrimFields {
    return MultiPrimFields{123, true, 4.56};
}

fn buildPrimAndRefFields() -> PrimAndRefFields {
    return PrimAndRefFields{true, "foo", buildOneCustomRefTypeField()};
}
