//
// Test constructor and basic accessor usage
//

struct NumWithText {
    number: i64,
    text: string,
}

// use constructor
fn createNumWithText() -> NumWithText {
    return NumWithText{42, "fourtytwo"};
}

// get a field
fn getTheNumberField(nwt: NumWithText) -> i64 {
    return nwt.number;
}

// get another field
fn getTheTextField(nwt: NumWithText) -> string {
    return nwt.text;
}

// use setters to change both fields
fn changeTo43(nwt: NumWithText) {
    nwt.number = 43;
    nwt.text = "fourtythree";
}
