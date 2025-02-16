# Klang Programming Language - Features and Syntax

## Program Structure
A Klang file consists of a list of **function** and **structured data type definitions**. The order in
which definitions appear does not matter.

The optional program **entry point** is a function with the signature
`___main___() -> void` and can be defined as follows:
```
fn ___main___() {
    //...
}
```
If the generated code will be used in context of a C or Java program (i.e. as a 
'library') the entry point should be left undefined, since it would collide with 
those entry points.

> More about defining regular functions can be found in the [function definition](#function-definition) section.

Whitespace is ignored in general.

## Types

### Primitives
* `i64` - 64-bit signed integer
    - Literal example: `123`

* `f64` - Double-precision floating point number
    - Literal example: `1.23`

* `bool` - Boolean
    - Literals: `true`, `false`

* `void` - Pseudo-type representing the absence of a (return) value

### Reference Types
* `string` - String
    - Literal example: `"some text"`
    - Supports following escape sequences:
        * `\"` - Double quotation mark (`"`)
        * `\\"` - Single backslash (`\`)
        * `\n` - Newline character aka. line feed (`LF`)
        * `\t` - Tab character
        * `\r` - Carriage return character (`CR`)

* User defined [structured data types](#structured-data-definition)


## Function Definition
```
fn {funName}( [{paramName}: {paramType}, ]* ) -> returnType {
    // statements...
}
```
* `{funName}` - The function's name
* `[{paramName}: {paramType}, ]*` - An (optional) comma separated list of parameter definitions
* `-> returnType` - The return type of the function
    > Can be omitted for `void` functions.
* Example
    ```
    fn foo(bar: i64) -> string {
        //...
    }
    ```
> Trailing comma in the parameter list is permitted. 

## Structured Data Definition
```
struct {structName} {
    {fieldDefinitions}?
    ---
    {methodDefinitions}?
}
```

* `{fieldDefinitions}?` - Optional comma separated list of field definitions.
    > Trailing comma in the field list is permitted. 
    - A field is defined as: `{fieldName}: {fieldType}`

* `{methodDefinitions}?` - Optional list of method definitions.
    - Definitions similar to [functions](#function-definition), except for __no__ `fn` keyword.
    - All methods have an __implicit__ `self` parameter, providing access to the struct instance.

> Definition-separator `---` must only be present if fields _and_ methods are defined. Otherwise it may be omitted.

* Example
    ```
    struct MyStruct {
        bar: i64
        ---
        foo(additional_bar: i64) -> void {
            self.bar = self.bar + additional_bar;
        }
    }
    ```

See also the related
* [`field assignment`](#field-assignment)
* [`constructor`](#struct-constructor)
* [`field getter`](#field-getter)
* [`method call`](#method-call)


## Statements
In general, there are two statement kinds:
* 'simple': Each of these are followed by a semicolon `;`.
* 'block-like': These contain a list of statements enclosed in curly braces `{...}`.

### Declaration & Assignment
Variables are declared using the `let` keyword, followed by the variable name.
If the declaration does not include an initializing expression, the name must be
followed by a type. Otherwise the type can be omitted and will be inferred from
the provided expression.

In summary there are these four patterns to declare and/or assign a variable:
* `let varName: varType;`
* `let varName: varType = {expr};`
    > Declared type and expression's type must be compatible.
* `let varName = {expr};`
    > Type is inferred from `{expr}`
* `varName = {expr};`

Also note:
* Reassignment is permitted as long as `{expr}` has a compatible type.
* Redeclaration of an existing variable is not allowed and will be rejected.

### Return
`return {expr};` - Return flow control to the caller, optionally returning a value.

### Break
`break;` - Exit the _current_ `loop` context.

### Drop
> If the compilation target is JBC/JVM this mechanic is optional (and basically
> a 'nop') because of the managed nature of the JVM.

`drop {expr};` - Release memory of a reference type instance given by `{expr}`.

In general Klang is an unmanaged language, thus the programmer is responsible
of freeing dynamically allocated memory. This is the case for all reference 
type instances __except string literals__.

> The tool `valgrind` can help find memory leaks in context of the final binary
> executable (e.g. `a.out`):
>   ```sh
>   valgrind --leak-check=yes ./a.out
>   ```


### Field assignment
```
{structInstanceExpr}.{fieldName} = {expr};
```
To set a field's value write a `.` after an expression evaluating to a struct instance
followed by the field's name. Then assign a value.

* Example
    ```
    myStruct.myField = 123;
    ```


### If-Else
```
if {condition} {
    // then do sth.
} else {
    // do sth. else
}
```
- `{condition}` - Expression evaluating to a boolean value

### Loop
Simple loop, repeating indefinitely if not explicitly stopped.
```
loop {
    //...
}
```
> Use `break` or a `return` statements to exit the loop.




## Expressions

### Literals
> see [Types](#types)

* `null` - The null literal representing the absence of a reference type value.

### Binary Operators
`{lhs} o {rhs}` where `o` is one of the following operators.

#### Arithmetic
> `{lhs}` and `{rhs}` must be numerical for these.
* `+` - add
* `-` - subtract
* `*` - multiply
* `/` - divide
* `%` - modulo (integral types only)

#### Comparison Operators
* `==`, `!=` - (in-)equality comparison
    - value based for primitives
    - identity/pointer based for reference types

> `{lhs}` and `{rhs}` must be numerical for these.
* `>`, `>=` - greater than (or equal)
* `<`, `<=`- less than (or equal)

#### Boolean/Logical Operators
> `{lhs}` and `{rhs}` must be boolean for these.
* `&&` - logical `and`
* `||` - logical `or`


### Unary Operators

#### Arithmetic
* `- {expr}` - negation, supported for numerical `{expr}`

#### Boolean/Logical Operators
* `! {expr}` - logical `not`, supported for boolean `{expr}`


### Type Casting
```
{expr} as {targetType}
```
* `{expr}` - The expression whose resulting type to cast.
* `{targetType}` - The type to convert the expression into.
* Example
    ```
    1.23 as i64
    ```

Some types can be casted into each other using the `as` keyword.
Bare in mind that this conversion will silently truncate types as necessary. For
example only the integral part will remain when `f64` is converted to `i64`.

Currently **only numerical types** support casting.

The type cast expression has a precedence higher than binary operator expressions
and lower than unary operator expressions.


### Ternary Conditional Operator
```
{condition} ? {thenExpr} : {elseExpr}
```
- `{condition}` - Expression evaluating to a boolean value
- `{thenExpr}` - Expression to evaluate to if `{condition}` was `true`
- `{elseExpr}` - Expression to evaluate to if `{condition}` was `false`

### Variables
Simply write the variable's or (in case of a function definition) parameter's name.

### Function Call
```
{funName} ( [expr, ]* )
```
Function name followed by an argument expression list as required by the
function's definition.

### Struct Constructor
```
{structName} { [expr, ]* }
```
Struct's name followed by a list of expressions inside curly braces (`{`, `}`).
Each expression corresponds to a field of the struct as implicated by the
definition order and values for all fields must be provided upon construction.

* Example assuming a definition `struct MyStruct{ foo: i64, bar: f64 }`
    ```
    MyStruct{123, 4.56}
    ```

### Field Getter
```
{structInstanceExpr}.{fieldName}
```
To get a field's value use `.` after an expression evaluating to a struct instance
followed by the field's name.

* Example
    ```
    StructWithStructField.myStructField.myField
    ```

### Method Call
```
{structInstanceExpr}.{methodName}({args}?)
```
To call a method value use `.` after an expression evaluating to a struct instance
followed by the method's name and the list of the required arguments.

* Example
    ```
    myStruct.foo(123, 456)
    ```

## Builtin Functions
* `print(value: {T}) -> void` - Print a value to stdout.
    - `{T}` - Currently implemented for `i64`, `f64`, `bool`, `string`
    - Example call `print(123);`

## Comments
* `//` - Line comment. Discards any following character in the current line.
* `/* ... */` - Block comment. Discards any character enclosed by `/*` and `*/`.

