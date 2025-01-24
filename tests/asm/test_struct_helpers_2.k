// Useful to test e.g. generated "to_string"

struct MyStruct {
    hello: i64,
    world: string,
}

struct WrapperStruct {
    obj: MyStruct
}


struct UseOtherStruct {
    hello: i64,
    the_struct_field_1: MyStruct,
    world: string,
    the_struct_field_2: MyStruct,
    especially_special_field: WrapperStruct,
}
