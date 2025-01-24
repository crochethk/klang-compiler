//
// Test destructor call generation
// - Probably the assembly code must be examined manually on whether the 
//      destructor call was generated properly
//

// ----------[ Struct Defs ]----------

struct Empty {}
fn dropEmpty(obj: Empty) { drop obj; }

struct OnePrimField {
    aInt: i64,
}
fn dropOnePrimField(obj: OnePrimField) { drop obj; }

struct OneCustomRefTypeField {
    struct_field: OnePrimField,
}
fn dropOneCustomRefTypeField(obj: OneCustomRefTypeField) { drop obj; }

struct OneStringField {
    string_field: string,
}
fn dropOneStringField(obj: OneStringField) { drop obj; }

struct MultiPrimFields {
    aInt: i64,
    aBool: bool,
    aFloat: f64,
}
fn dropMultiPrimFields(obj: MultiPrimFields) { drop obj; }

struct PrimAndRefFields {
    aBool: bool,
    aStr: string,
    aStruct: OneCustomRefTypeField,
}
fn dropPrimAndRefFields(obj: PrimAndRefFields) { drop obj; }

/** --- Example C code to call the functions ---

#include "tests.asm.test_drop_stat.h"

int main() {
    int i = 1;
    struct Empty* empty = Empty$new$();
    struct OnePrimField* oneprim = OnePrimField$new$(123L);
    struct OneCustomRefTypeField* onecustom = OneCustomRefTypeField$new$(OnePrimField$new$(456L));
    struct OneStringField* onestring = OneStringField$new$("hello world");
    struct MultiPrimFields* multiprim = MultiPrimFields$new$(789L, true, 1.23d);
    struct PrimAndRefFields* primandref = PrimAndRefFields$new$(true, "abc", OneCustomRefTypeField$new$(OnePrimField$new$(123)));

    printf("drop%d\n", i++);
    dropEmpty(empty);
    printf("drop%d\n", i++);
    dropOnePrimField(oneprim);
    printf("drop%d\n", i++);
    dropOneCustomRefTypeField(onecustom);
    printf("drop%d\n", i++);
    dropOneStringField(onestring);
    printf("drop%d\n", i++);
    dropMultiPrimFields(multiprim);
    printf("drop%d\n", i++); // 6
    dropPrimAndRefFields(primandref);
}
*/
