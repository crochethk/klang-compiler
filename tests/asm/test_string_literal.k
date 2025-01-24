/* return string simple literal */
fn stringLit_hello_world() -> string {
    return "hello world";
}

/* return string literal with utf8 escaped properly */
fn stringLit_ae_oe_ue_ss_euro() -> string {
    // expected asm const: "\303\244\303\266\303\274\303\237\342\202\254"
    return "äöüß€";
}

/* return string literal with utf8 escaped properly */
fn stringLit_spaecial() -> string {
    // expected asm const: "sp\303\244cial"
    return "späcial";
}

/* return string literal with new line character */
fn stringLit_withNLnew_line() -> string {
    // expected asm const: "with\012\011new line"
    return "with\n\tnew line";
}

