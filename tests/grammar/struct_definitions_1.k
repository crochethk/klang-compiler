/**
 * Try out different styles of struct definitions.
 */

// Only Fields
struct Foo1_1 {
    field1: i64,
    field2: i64, // "," optional
    --- //optional
}
struct Foo1_2 {
    field1: i64,
    field2: i64,
    //---
}
struct Foo1_3 {
    field1: i64,
    field2: i64 //,
    ---
}
struct Foo1_4 {
    field1: i64,
    field2: i64 //,
    //---
}


// Only Methods
struct Foo2_1 {
    --- //optional
    meth1() {}
    meth2() {}
}
struct Foo2_2 {
    //---
    meth1() {}
    meth2() {}
}

// Fields + Methods
struct Foo3_1 {
    field1: i64,
    field2: i64, // "," optional
    ---
    meth1() {}
    meth2() {}
}
struct Foo3_2 {
    field1: i64,
    field2: i64
    ---
    meth1() {}
    meth2() {}
}

// Neither
struct Foo4_1 {
    ---//optional
}
struct Foo4_2 {
    //---
}
