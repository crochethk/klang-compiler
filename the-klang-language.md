# Klang Programming Language - Features and Syntax

## Program Structure
A Klang file consists of a list of **function** and **structured data type definitions**. The order in
which definitions appear does not matter.

The optional **entry point** is a function with the signature
`___main___() -> void` and can be defined as follows:
```
fn ___main___() {
    //...
}
```
> More about defining regular functions can be found in the [function definition](#function-definition) section.

Whitespace is ignored in general.

## Types

### Primitves
* `i64` - 64-bit signed integer
    - Literal example: `123`

* `f64` - Double-precision floating point number
    - Literal example: `1.23`

* `bool` - Boolean
    - Literals: `true`, `false`

* `void` - Pseudotype representing the absence of a (return) value

### Reference Types
* `string` - String
    - Literal example: `"some text"`
    - Supports following escape sequences:
        * `\"` - Double quotation mark (`"`)
        * `\\"` - Single backslash (`\`)
        * `\n` - Newline character aka. line feed (`LF`)
        * `\t` - Tab character
        * `\r` - Carriage return charachter (`CR`)

* Userdefined [structured data types](#structured-data-definition)


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
...
<!-- TODO TODO TODO -->

## Statements
In general, there are two statement kinds:
* 'simple': Each of these are followed by a semicolon `;`.
* 'block-like': These contain a list of statments enclosed in curly braces `{...}`.

### Defining Variables
Variables are declared using the `let` keyword, followed by the variable name.
If the declaration dose not include an initializing expression, the name must be
followed by a type. Otherwise the type can be omitted and will be inferred from
the provided expression.

In summary there are these four patterns to declare and/or assign a variable:
* `let varName: varType;`
* `let varName: varType = {expr};` 
    > Declared type and expression's type must be compatible.
* `let varName = {expr};` 
    > Type is inferred from `{expr}`
* `varName = {expr};`

### Return
...
<!-- TODO TODO TODO -->

### Break
...
<!-- TODO TODO TODO -->

### Drop
> If the compilation target is JBC/JVM this mechanic is optional (and basically
> a 'nop') because of the managed nature of the JVM.

In general Klang is an unmanaged language, thus the programmer is responsible
of freeing dynamically allocated memory. This is the case for all reference 
type instances except string literals.

...
<!-- TODO TODO TODO -->

### Field assignment
...
<!-- TODO TODO TODO -->


### If-Else
```
if {condition} {
    // then do smth.
} else {
    // do smth. else
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


### Ternary Conditional Operator
```
{condition} ? {thenExpr} : {elseExpr}
```
- `{condition}` - Expression evaluating to a boolean value
- `{thenExpr}` - Expression to evaluate to if `condition` was `true`
- `{elseExpr}` - Expression to evaluate to if `condition` was `false`

### Variables
Simply write the variable's or (in case of a function defintion) parameter's name.

### Function Call
```
{funName}( [expr, ]* )
```
Function name followed by an argument expression list as required by the
function's definition.

### Field Getter
...
<!-- TODO TODO TODO -->

### Method Call
...
<!-- TODO TODO TODO -->


## Builtin Functions
* `print(value: {T}) -> void` - Print a value to stdout.
    - `{T}` - Currently implemented for `i64`, `f64`, `bool`, `string`
    - Example call `print(123);`

## Comments
* `//` - Line comment
* `/* ... */` - Block comment
> The last line of a Klang file must not be a commented out.

